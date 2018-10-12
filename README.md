# Preql

[![Build Status](https://travis-ci.org/ILLGrenoble/preql.svg?branch=master)](https://travis-ci.org/ILLGrenoble/preql)

Preql (Predicate query language) is a project designed to filter JPA collections using client-side expressions.

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

**Javadocs**

https://illgrenoble.github.io/preql

### Getting started

**Defining a filter query provider for an entity**

Before you can start filtering a collection, you must first define a filter query provider for a given entity class. 

There are two types of fields that can be defined:

 -	`field` A field that can be queried but cannot be ordered (useful if you don't want to order on a sub collection)
 -	`orderableField` A field that can be both queried and ordered

In the code below, we are defining a provider for the `Course` entity and registering the fields. Fields that can be queried must be **explicitly** defined.

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
            	parameter
                orderableField("price", new CurrencyFieldValueParser()),
                parameter
                orderableField("duration", new DurationFieldValueParser()),
                orderableField("startDate"),
                orderableField("endDate"),
            	// a field that belongs to the tags association
                field("tags.name", "tags"),
            	// fields that belongs to the teacher association
                field("teacher.name"),
                field("teacher.age"),
            	// fields that belong to the attachments association
                field("attachments.size", new ByteFieldValueParser()),
                field("attachments.name")
        );
    }
}
```

**Attribute paths**

A field attribute path must correspond directly to an attribute path in the object entity graph. Let's say we have an entity of `Course` with an associated collection on `Tag`. To query the tag name we would define the following:

```java
field("tags.name")
```

Preql will traverse the object graph and check if the attribute exists and throw an error if it doesn't. It will also add a join to the `tags` table.

**Avoiding duplicate joins**

To avoid duplicate joins, Preql keeps tracks of joins already added to the criteria.  For example, lets say we have defined two fields on an association:

```java
field("attachments.size"),
field("attachments.name")
```

It will check if a join already exists for `attachments` on `course`. If it doesn't, it will be added, but if it does then the pre-existing join will be used.

**Aliasing fields**

You can define an alias for a field. The alias can be anything but the attribute path (in this case `tags.name`) must correspond directly to a valid attribute path in the object entity graph.

```java
// define the path attribute as tags.name with an alias of tags
orderableField("tags.name", "tags")
```

This would allow the user to do `tags IN :tags` instead of `tags.name IN :tags` when querying the collection.


**Creating the query**

```java
/* 
	Pass the entity manager to the factory. 
	Normally you'd use guice or spring injection to instantiate the object
*/
final CourseQueryProvider provider = new CourseQueryProvider(em());

// Create a new query
final String preql = "tags = :tags AND active = :active";
final FilterQuery<Course> query = provider.createQuery(preql);
```

**Pagination**

An offset and limit can also be defined by calling the `setPagination` method on the query and passing in a `Pagination` object. The pagination objects accepts an `offset` and `limit`.

Filter the results with an `offset` of *0* and a limit of *100*.

```
// the pagination function is a just a helper function
query.setPagination(pagination(0, 100));
// you could also do
query.setPagination(new Pagination(0, 100));
```

**Parameters**

Given the query of `tags IN :tags AND active = :tags` 

To bind the parameters to this query, we would do:

```java
query.setParameter("active", true)
     .setParameter("tags", asList("computing", "mathematics"))
// you could also pass in a map
Map<String, Object> parameters = new HashMap<String, Object>() {{
    put("active", true);
    put("tags", asList("computing", "mathematics"));
}};
query.setParameters(parameters);
```

**Ordering the results**

You can order the results by calling the `setOrder` method on the query. Only fields that have been defined as `orderable` can be ordered, otherwise an exception will be thrown.

```java
query.setOrder("id", "desc");
```

**Constraints**

Constraints are useful if you want to filter a collection based on an condition **not** provided by the user. For example, let's say a user can only see courses for their given organisation (tenant).  The `addConstraint` method on the query object provides a callback with the parameters of `criteriaBuilder`, `criteria`, `root` (and in our case the `Course` entity) and the`entityManager`.

```java
final String tenantId = 1;
query.addConstraint(
    (criteriaBuilder, criteria, root, entityManager) -> {
     return criteriaBuilder.equal(root.get("tenant").get("id"), tenantId);
    }
);
```

**Complex constraints**

You can also define complex constraints. Let's say you want to define a sub-query. There are two ways:

- Build the sub-query using the criteria builder (more verbose, but only one query when executed)
- Build the sub-query using JPQL directly (two queries are executed but it's easier to read).

The example below creates a sub-query for selecting all ids from the tenant table where the tenant name is either *Tenant 1* or *Tenant 2*

Criteria builder:

```java
query.addConstraint((cb, criteria, root, entityManager) -> {
    final Subquery<Long> subquery   = criteria.subquery(Long.class);
    final Root<Tenant>   tenantRoot = subquery.from(Tenant.class);
    subquery.from(Tenant.class);
    subquery.where(tenantRoot.get("name").in(asList("Tenant 1", "Tenant 2")));
    subquery.select(tenantRoot.get("id"));
    return root.get("tenant").get("id").in(subquery);
});
```

JPQL:

```java
query.addConstraint((cb, criteria, root, entityManager) -> {
    final String jpql = "SELECT t.id FROM Tenant t WHERE t.name IN (:name)";
    final Query tenantQuery = entityManager.createQuery(jpql);
    tenantQuery.setParameter("name", asList("Tenant 1", "Tenant2"));
    final Collection identifiers = tenantQuery.getResultList();
    return root.get("tenant").get("id").in(identifiers);
});
```

The above example is just for demo purposes. For the above query, you would probably do:

```java
query.addConstraint((cb, criteria, root, entityManager) -> {
    return root.get("tenant").get("name").in(asList("Tenant 1", "Tenant2"));
});
```

**Limiting the number of expressions**

You can limit the number of expressions that are defined by calling the `setMaxExpresions(n)` method. By default, there is no limit, so we recommend to set it.

```java
query.setMaxExpressions(10);
```

**Executing the query**

Let's put all of this together. What we want:

 - Find all courses that are *active* and have a tag of *computing* or *mathematics*
 - Paginate the results with an offset of *10* and a limit of *100*
 - Order the results by the *course name* in *ascending order*
 - Filter the courses by a *tenant id*
 - Return a list of all courses that matches the above criteria

```java
final CourseQueryProvider provider = new CourseQueryProvider(em());
final FilterQuery<Course> query = provider.createQuery("tags IN :tags AND active = :active");
query.setParameter("tags", asList("computing", "mathematics"))
     .setParameter("active", true)
	 .addConstraint((cb, criteria, root, entityManager) ->
      	cb.equal(root.get("tenant").get("id"), 1)
      )
     .setPagination(pagination(10, 100))
     .setOrder("name", "asc");
// Get the results
return query.getResultList();
```

> Use `getSingleResult` for a single result,  `getResultStream` for a stream or `count` to count the number of records.

For a complete example, please check out the tests.

### Parameter parsers

Parameters parsers are used to parse a parameter value to the corresponding fields attribute type. 

**Example**

The entity `Course` has a property (attribute) of `credits` with a type of `Integer`.

For the given query:

```java
final CourseQueryProvider provider = new CourseQueryProvider(em());
final FilterQuery<Course> query = provider.createQuery("credits >= :credits");
query.setParameter("credits", "200")
return query.getResultList();
```

When the query is being parsed, it will look up to see what the property type is of the parameter being queried. In this example:  `:credits` 

It will try to coerce the parameter value into the property type.  Even though, we have given the parameter value as a string, it will try to convert that string into an `Integer`. An exception will be thrown If the parameter cannot be converted into the property type.

**Parameter parsers out of the box matrix**

| Parameter parser          | Description                                      |
| --------------------- | ------------------------------------------------ |
| BigDecimalParameterParser | Convert an object value into a big decimal       |
| BooleanParameterParser    | Convert an object value into a boolean           |
| ByteParameterParser       | Convert an object value into a byte decimal      |
| CharacterParameterParser  | Convert an object value into a character decimal |
| DateParameterParser       | Convert an object value into a date object (more information below) |
| DoubleParameterParser     | Convert an object value into a double            |
| FloatParameterParser      | Convert an object value into a float             |
| IdentityParameterParser   | Returns the value as-is (no conversion)          |
| IntegerParameterParser    | Convert an object value into an integer          |
| LongParameterParser       | Convert an object value into a long              |
| ShortParameterParser      | Convert an object value into a short             |
| StringParameterParser     | Convert an object value into a string            |
| UUIDParameterParser       | Convert an object value into a UUID              |

**Date parsing**

The date parser accepts the two following formats:

- `yyyy-MM-dd'T'HH:mm:ss`
- `yyyy-MM-dd`

A custom format can be added by calling:

```java
DateValueParser.registerFormat(format) // for example YYYY
```

**Defining a custom parameter parser** 

This parameter parser will convert a given object into a UUID (this parameter parser already exists, just for demo purposes). 

```java
/**
 * Convert an object into a UUID
 */
public class UUIDValueParser implements ValueParser<UUID> {

    private static final String TYPE_UUID = "uuid";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                UUID.class,
                UUID.class.getName(),
                TYPE_UUID
        };
    }

    @Override
    public UUID parse(final Object value) {
        try {
            if (value instanceof UUID) {
                return (UUID) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                return fromString(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a UUID", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a UUID", value));
    }
}

```

Register it

```java
ParameterParsers.registerParser(new UUIDValueParser());
```

Now any property that has a type of `UUID` will be coerced using this parser.

### Custom field parsers

A custom field value parser can be added to any field. 

Here is an example that converts a currency expression into US dollars (i.e. 1GBP, 2EUR etc.)

```java
/**
 * Convert EUR or GBP into dollars
 */
public class CurrencyFieldParser implements FieldParser<Double> {
	/**
	 * For demo purposes, we are explicitly defining the conversion rates
	 * In a real world application, this would be dynamic.
	 */
    private final static double EUR_RATE = 1.12;
    private final static double GBP_RATE = 1.30;

    /**
     *parameter
     */
    private ValueParser<Double> fallbackValueParser = new DoubleValueParser();

    @Override
    public Double parse(final Object value) {
        if (value instanceof String) {
            final Pattern pattern = compile(parameter);
            final Matcher matcher = pattern.matcher((String) value);
            if (matcher.matches()) {
                double ret = parseDouble(matcher.group(parameter));
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
field("price", new CurrencyFieldParser())
```

### Expressions

Preql supports the following expressions.

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

Here is a list of some example queries. You can find more examples, look at the [FilterQueryTest](https://github.com/ILLGrenoble/preql/blob/master/src/test/java/eu/ill/preql/FilterQueryTest.java) file in the tests directory.

**Examples matrix**

| Query                                                       | Parameters                                      |
| ----------------------------------------------------------- | ----------------------------------------------- |
| `id = :id`                                                  | id: 1                                           |
| `id IS NOT NULL`                                            |                                                 |
| `id IS NULL`                                                |                                                 |
| `code = :code`                                              | code: "C-JAVA"                                  |
| `description LIKE :description`                             | description: "%discovering web%                 |
| `description NOT LIKE :description`                         | description: "%discovering web%                 |
| `price <= :price`                                           | price: 100.00                                   |
| `price <= :price`                                           | price: "90GBP"                                  |
| `duration = :duration`                                      | duration: "10HOURS"                             |
| `credits < :credits`                                        | credits: 1000                                   |
| `active = :active`                                          | active: false                                   |
| `id IN :ids`                                                | ids: [1,2,3,4]                                  |
| `id NOT IN :ids`                                            | ids: [1,2,3,4]                                  |
| `tags = :tags`                                              | tags: "programming"                             |
| `tags IN :tags`                                             | tags: ["programming", "computing"]              |
| `teacher.name = :teacher1 or  teacher.name = :teacher2`     | teacher1: "Jamie Hall",  teacher2: "Joe Bloggs" |
| `credits BETWEEN :lowerBound AND :upperBound`               | lowerBound: 1000, upperBound: 10000             |
| `credits NOT BETWEEN :lowerBound AND :upperBound`           | lowerBound: 1000, upperBound: 10000             |
| `attachments.size >= :size`                                 | size: "1MB"                                     |
| `attachments.size >= :size`                                 | size: 2000                                      |
| `attachments.name LIKE :name AND attachments.size >= :size` | name: "%.jpg", size: "10MB"                     |
| `startDate >= :startDate`                                   | startDate: "2017-01-01"                         |
| `startDate BETWEEN :startDate AND :endDate`                 | startDate: "2018-01-01", endDate: "2018-03-01"  |

### Use case

We wanted to give users the ability to filter [graphql](https://graphql.org/) collections using an expressive syntax.

```
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

**Grammar**

[Antlr4 grammar](https://github.com/ILLGrenoble/preql/blob/master/src/main/antlr4/eu/ill/preql/Filter.g4)

**Building**

`mvn clean compile`

**Tests**

The tests are written in Junit 5.

