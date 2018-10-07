/*
 * Copyright 2018 Institut Laue–Langevin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar Filter;

query :                     expression? EOF;

expression :                field operator=(LT | LT_EQ | GT | GT_EQ | EQ | NOT_EQ1 | NOT_EQ2) parameter   #comparatorExpression
                            | expression operator=(AND | OR) expression                                   #binaryExpression
                            | field (NOT)? BETWEEN parameter AND parameter                                #betweenExpression
                            | field (NOT)? IN parameter                                                   #inExpression
                            | field (NOT)? LIKE parameter                                                 #likeExpression
                            | field IS (NOT)? NULL                                                        #nullExpression
                            | OPEN_PAR expression CLOSE_PAR                                               #basicQuery
                            ;

field :                     IDENTIFIER;
parameter :                 ':' IDENTIFIER;

SCOL :                      ';';
DOT :                       '.';
OPEN_PAR :                  '(';
CLOSE_PAR :                 ')';
COMMA :                     ',';
LT :                        '<';
LT_EQ :                     '<=';
GT :                        '>';
GT_EQ :                     '>=';
EQ :                        '=';
NOT_EQ1 :                   '!=';
NOT_EQ2 :                   '<>';
BETWEEN :                   B E T W E E N;
AND :                       A N D  | '&&';
OR :                        O R | '||';
NOT :                       N O T;
IN :                        I N;
LIKE :                      L I K E;
IS :                        I S;
NULL :                      N U L L;
IDENTIFIER :                [a-zA-Z_] [a-zA-Z_0-9.]*;
SINGLE_LINE_COMMENT :       '--' ~[\r\n]* -> channel(HIDDEN);
MULTILINE_COMMENT :         '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN);
SPACES:                     [ \u000B\t\r\n] -> channel(HIDDEN);
UNEXPECTED_CHAR:            . ;

fragment DIGIT :            [0-9];
fragment A :                [aA];
fragment B :                [bB];
fragment C :                [cC];
fragment D :                [dD];
fragment E :                [eE];
fragment F :                [fF];
fragment G :                [gG];
fragment H :                [hH];
fragment I :                [iI];
fragment J :                [jJ];
fragment K :                [kK];
fragment L :                [lL];
fragment M :                [mM];
fragment N :                [nN];
fragment O :                [oO];
fragment P :                [pP];
fragment Q :                [qQ];
fragment R :                [rR];
fragment S :                [sS];
fragment T :                [tT];
fragment U :                [uU];
fragment V :                [vV];
fragment W :                [wW];
fragment X :                [xX];
fragment Y :                [yY];
fragment Z :                [zZ];
