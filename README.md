# Preql

Preql (Predicate query language) is a project designed to filter JPA collections through client-side expressions.

This library provides a custom SQL grammar that is converted to a JPA [Criteria Query](http://docs.oracle.com/javaee/6/tutorial/doc/gjitv.html) (object representation of JPQL), which is translated to an SQL query.  The initial use case for Preql was to provide filtering of collections using GraphQL. It can, however, be used for any other client side implementations that requires collection filtering (REST for example).

Feel free to contribute!

###  Installation

The minimum JDK required is 8.

**Maven**

> Waiting to be deployed on maven central

```xml
<dependency>
    <groupId>eu.ill</groupId>
    <artifactId>preql</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Example

A Spring Boot integration example is coming soon.

### Getting started

**Defining a filter query provider for an entity**

Before you can start filtering a collection, you must first define a query provider for a given entity class. 

There are two types of fields that can be defined:

 -	`field` A field that can be queried but cannot be ordered (useful if you don't want to order on a sub collection)
 -	`orderableField` A field that can be both queried and ordered by

In the code below, we are defining a provider for the `Course` entity and registering the fields. Fields that can be queried must be **explicitliy** defined.

```java
public class CourseFilterQueryProvider extends AbstractFilterQueryProvider<Course> {
    public CourseFilterQueryProvider(EntityManager entityManager) {
        super(Course.class, entityManager);
        addFields(
                orderableField( "id"),
                orderableField("name"),
                orderableField("description"),
                orderableField("code"),
                orderableField("active"),
                orderableField("credits"),
            	// a custom field value parser for the price field
                orderableField("price", new CurrencyFieldValueParser()),
                // a custom field value parser for the duration field
                orderableField("duration", new DurationFieldValueParser()),
                orderableField("startDate"),
                orderableField("endDate"),
            	// a field that belongs to the tags association
                field("tags.name", "tags"),
            	// fields that belongs to the teacher association
                field("teacher.name"),
                field("teacher.age"),
                field("teacher.affiliation.name"),
            	// fields that belong to the attachments association
                field("attachments.size", new ByteFieldValueParser()),
                field("attachments.name")
        );
    }
}
```

**Attribute paths**

A fiield attribute path must correspond directly to an attribute path in the object entity graph. Let's say we have an entity of `Course` with an associated collection on `Tag`. To query the tag name we would define the following:

```java
field("tags.name")
```

Preql will automatically traverse the object graph and check if the attribute exists and throw an error if it doesn't. It will also automatically add a join to the `tags` table.

**Avoiding duplicate joins**

To avoid duplicate joins, Preql automatically keeps tracks of joins already added to the criteria.  For example, lets say we have defined two fields on an association:

```java
field("attachments.size"),
field("attachments.name")
```

It will check if a join already exists for `attachments` on `course`. If it doesn't, it will be added, but if it does then the pre-existing join will be used.

**Aliasing fields**

You can define an alias for the field. The alias can be anything but the attribute path (in this case `tags.name`) must correspond directly to a valid attribute path in the object entity graph.

```java
// define the path attribute as tags.name with an alias of tags
orderableField("tags.name", "tags")
```

This would allow the user to do `tags IN :tags` instead of `tags.name IN :tags` when querying the collection.

**Custom field value parsers**

A custom field value parser can be added for a field. 

Here is an example that converts a currency expression into US dollars (i.e. 1GBP, 2EUR etc.)

```java
/**
 * Convert EUR or GBP into dollars
 */
public class CurrencyFieldValueParser implements FieldValueParser<Double> {
	/**
	 * For demo purposes, we are explicility defining the conversion rates
	 * In a real world application, this would be dynamic.
	 */
    private final static double EUR_RATE = 1.12;
    private final static double GBP_RATE = 1.30;

    /**
     * If we cannot convert, then fallback to this value parser
     */
    private ValueParser<Double> fallbackValueParser = new DoubleValueParser();

    @Override
    public Double parse(final Object value) {
        if (value instanceof String) {
            final Pattern pattern = compile("^(?<value>\\d+.\\d{0,2})(?<currency>GBP|EUR)$");
            final Matcher matcher = pattern.matcher((String) value);
            if (matcher.matches()) {
                double ret = parseDouble(matcher.group("value"));
                switch (matcher.group("currency")) {
                    case "GBP":
                        return ret * GBP_RATE;
                    case "EUR":
                        return ret * EUR_RATE;
                }
            }
            throw new InvalidQueryException(format("Could not parse '%s' into currency", value));
        }
        return fallbackValueParser.parse(value);
    }
}
```

Associate it to the field in the provider

```java
field("price", new CurrencyFieldValueParser())
```

**Creating the query**

```java
// Pass the entity manager to the factory. Normally you'd use guice or spring injection to instantiate the object
final CourseQueryProvider provider = new CourseQueryProvider(em());

// Create a new query
final FilterQuery<Course> query = provider.createQuery("tags = :tags AND active = :active");
```

**Pagination**

An offset and limit can also be defined by calling the `setPagination` method on the query and passing in a `Pagination` object.

```
// the pagination function is a just a helper function
query.setPagination(pagination(0, 100));
// you could also do
query.setPagination(new Pagination(0, 100));
```

**Parameters**

Given the query of `tags = :tags AND active = :tags` 

To bind the parameters to this query, we would do:

```java
query.setParameter("active", true)
     .setParameter("tags", "computing")
 // you could also pass in a map
 Map<String, Object> parameters = new HashMap<String, Object>() {{
    put("active", true);
    put("tags", "computing");
}};
query.setParameters(parameters);
```

**Ordering the results**

You can order the results by calling the `setOrder` method on the query. Only fields that have been defined as `orderable` can be ordered, otherwise an exception will be thrown.

```java
query.setOrder("id", "desc");
```

**Predefined expressions**

Predefined expressions are useful if you want to supplementaly filter a collection based on an expression **not** provided by the user. For example, let's say a user can only see courses for their given organisation (tenant).  The `addExpressions` method on the query object provides a callback with the parameters of `criteriaBuilder` and the `root` object (in our case, the `Course` entity)

```java
query.addExpression(
    (criteriaBuilder, root) -> criteriaBuilder.equal(root.get("tenant").get("id"), 1)
)
```

This expression will be added to the query after the query has been parsed.

**Executing the query**

Let's put all of this together. What we want:

 - Find all courses that are *active* and have a tag of *computing*
 - Paginate the results with an offset of *10* and a limit of *100*
 - Order the results by the *course name* in *ascending order*
 - Filter the courses by a *tenant id*
 - Return a list of all courses that matches the above criteria

```java
final CourseQueryProvider provider = new CourseQueryProvider(em());
final FilterQuery<Course> query = provider.createQuery("tags = :tags AND active = :active");
query.setParameter("tags", "*computing*")
     .setParameter("active", true)
     .addExpression((criteriaBuilder, root) ->
    	criteriaBuilder.equal(root.get("tenant").get("id"), 1)
	  )
     .setPagination(pagination(10, 100))
     .setParameter("tags", "computing")
     .setOrder("name", "asc");
// Get the results
return query.getResultList();
```

> Use `getSingleResult` for a single result or `count`, `getResultStream` for a stream or `count` to count the number of records.

For a complete example, please check out the tests.

**Limiting the number of expressions**

You can limit the number of expressions that are defined by calling the `setMaxExpresions(n)` method. By default, there is no limit, so we recommend to set it.

```java
QueryParser.setMaxExpressions(10);
```

### Value parsers

Value parsers are used to parse a parameter value to the corresponding fields attribute object type.  For example, if your entity has an attribute of `credits` with a type of `Long` then when the query is parsed, it will try to convert the given parameter value into a `Long`. An exception will be thrown if the parameter cannot be converted to match the attribute type.

**Value parsers out of the box matrix**

| Value parser          | Description                                      |
| --------------------- | ------------------------------------------------ |
| BigDecimalValueParser | Convert an object value into a big decimal       |
| BooleanValueParser    | Convert an object value into a boolean           |
| ByteValueParser       | Convert an object value into a byte decimal      |
| CharacterValueParser  | Convert an object value into a character decimal |
| DateValueParser       | Convert an object value into a date object       |
| DoubleValueParser     | Convert an object value into a double            |
| FloatValueParser      | Convert an object value into a float             |
| IdentityValueParser   | Returns the value as-is (no conversion)          |
| IntegerValueParser    | Convert an object value into an integer          |
| LongValueParser       | Convert an object value into a long              |
| ShortValueParser      | Convert an object value into a short             |
| StringValueParser     | Convert an object value into a string            |
| UUIDValueParser       | Convert an object value into a UUID              |

**Registering a value parser globally**

You can, for example, on application startup, register a value parser to be used in all instances of `FilterQuery`.

```java
FilterQuery.addValueParser(new LongValueParser());
```

**Defining a custom value parser** 

This value parser will convert a given object to a boolean (this value parser already exists, just for demo purposes).

```java
import InvalidQueryException;
import org.jetbrains.annotations.NotNull;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

public class BooleanValueParser implements ValueParser<Boolean> {

    private static final String BOOLEAN_TYPE = "boolean";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Boolean.class,
                Boolean.TYPE,
                Boolean.class.getName(),
                BOOLEAN_TYPE
        };
    }

    @Override
    public Boolean parse(@NotNull final Object value) {
        try {
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            final String v = value.toString();
            if (v.trim().length() == 0) {
                return false;
            } else {
                return parseBoolean(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a boolean", value));
        }
    }
}
```

Register it

```
QueryParser.addValueParser(new BooleanValueParser());
```

### Expressions

**Expression matrix**

| Expression    | Description               | Example           |
| ------------- | ------------------------- | ----------------- |
| =             | Equals                    | id = 1            |
| >=            | Greater than or equal to  | id >= 1           |
| >             | Greater than              | id > 1            |
| <             | Less than                 | id <1             |
| <=            | Less than or equal to     | id <= 1           |
| !=            | Not equal to              | id != 1           |
| <>            | Not equal to              | id <> 1           |
| (NOT) IN      | Equality, multiple values | IN (1, 2, 3, 4)   |
| (NOT) BETWEEN | Between, numerical range  | BETWEEN 20 AND 50 |
| IS NULL       | Nullable                  | id IS NULL        |
| IS NOT NULL   | Not nullable              | id IS NOT NULL    |

### Example queries

Here is a list of some example queries. You can find more examples in the tests directory.

**Examples matrix**

| Query                                                   | Parameters                                      |
| ------------------------------------------------------- | ----------------------------------------------- |
| `id = :id`                                              | id: 1                                           |
| `id IS NOT NULL`                                        |                                                 |
| `id IS NULL`                                            |                                                 |
| `code = :code`                                          | code: "C-JAVA"                                  |
| `description LIKE :description`                         | description: "%discovering web%                 |
| `description NOT LIKE :description`                     | description: "%discovering web%                 |
| `price <= :price`                                       | price: 100.00                                   |
| `price <= :price`                                       | price: "90GBP"                                  |
| `duration = :duration`                                  | duration: "10HOURS"                             |
| `credits < :credits`                                    | credits: 1000                                   |
| `active = :active`                                      | active: false                                   |
| `id IN :ids`                                            | ids: [1,2,3,4]                                  |
| `id NOT IN :ids`                                        | ids: [1,2,3,4]                                  |
| `tags = :tags`                                          | tags: "programming"                             |
| `tags IN :tags`                                         | tags: ["programming", "computing"]              |
| `teacher.name = :teacher1 or  teacher.name = :teacher2` | teacher1: "Jamie Hall",  teacher2: "Joe Bloggs" |
| `credits BETWEEN :lowerBound AND :upperBound`           | lowerBound: 1000, upperBound: 10000             |
| `credits NOT BETWEEN :lowerBound AND :upperBound`       | lowerBound: 1000, upperBound: 10000             |
| `attachments.size >= :size`                             | size: "1MB"                                     |
| `attachments.size >= :size`                             | size: 2000                                      |
| `attachments.name LIKE :name AND size >= :size`         | name: "%.jpg", size: "10MB"                     |
| `startDate >= :startDate`                               | startDate: "2017-01-01"                         |
| `startDate BETWEEN :startDate AND :endDate`             | startDate: "2018-01-01", endDate: "2018-03-01"  |

### Use case

We wanted to give users the ability to filter graphql collections using an expressive syntax.

```json
query {
	courses(filter: { 
		query: "tags IN :tags AND startDate >= startDate", 
		params: { "tags": ["computing", "programming"], "startDate": "2018-01-01H00:00:00"  
	}) {
		name
		duration
		startDate
		tags
	}
}
```

### Development

**Building**

`mvn clean compile`

**Tests**

The tests are written in Junit 5

