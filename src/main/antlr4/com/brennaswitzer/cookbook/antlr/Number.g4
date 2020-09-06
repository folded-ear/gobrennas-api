grammar Number;

start
    :   a=atom
        (AND b=atom)*
    ;

atom
    :   number
    |   name
    |   fraction
    ;

number
    :   i=integer
        (AND? f=fraction)?
    |   d=decimal
    ;

integer
    :   INTEGER
    ;

decimal
    :   DECIMAL
    ;

fraction
    :   n=integer SLASH d=integer
    |   vf=vulgarFraction
    ;

vulgarFraction returns [double val]
    : f=VULGAR_FRACTION {
        switch ($f.text) {
            case "¼":
                $val = 1.0 / 4.0;
                break;
            case "½":
                $val = 1.0 / 2.0;
                break;
            case "¾":
                $val = 3.0 / 4.0;
                break;
            case "⅐":
                $val = 1.0 / 7.0;
                break;
            case "⅑":
                $val = 1.0 / 9.0;
                break;
            case "⅒":
                $val = 1.0 / 10.0;
                break;
            case "⅓":
                $val = 1.0 / 3.0;
                break;
            case "⅔":
                $val = 2.0 / 3.0;
                break;
            case "⅕":
                $val = 1.0 / 5.0;
                break;
            case "⅖":
                $val = 2.0 / 5.0;
                break;
            case "⅗":
                $val = 3.0 / 5.0;
                break;
            case "⅘":
                $val = 4.0 / 5.0;
                break;
            case "⅙":
                $val = 1.0 / 6.0;
                break;
            case "⅚":
                $val = 5.0 / 6.0;
                break;
            case "⅛":
                $val = 1.0 / 8.0;
                break;
            case "⅜":
                $val = 3.0 / 8.0;
                break;
            case "⅝":
                $val = 5.0 / 8.0;
                break;
            case "⅞":
                $val = 7.0 / 8.0;
                break;
        }
    };

name returns [double val]
    :   n=NAME {
        switch ($n.text.toLowerCase()) {
            case "one half" :
                $val = 0.5;
                break;
            case "half"     :
                $val = 0.5;
                break;
            case "one"      :
                $val = 1.0;
                break;
            case "two"      :
                $val = 2.0;
                break;
            case "three"    :
                $val = 3.0;
                break;
            case "four"     :
                $val = 4.0;
                break;
            case "five"     :
                $val = 5.0;
                break;
            case "six"      :
                $val = 6.0;
                break;
            case "seven"    :
                $val = 7.0;
                break;
            case "eight"    :
                $val = 8.0;
                break;
            case "nine"     :
                $val = 9.0;
                break;
            case "ten"      :
                $val = 10.0;
                break;
            case "eleven"   :
                $val = 11.0;
                break;
            case "twelve"   :
                $val = 12.0;
                break;
            case "thirteen" :
                $val = 13.0;
                break;
            case "fourteen" :
                $val = 14.0;
                break;
            case "fifteen"  :
                $val = 15.0;
                break;
            case "sixteen"  :
                $val = 16.0;
                break;
            case "seventeen":
                $val = 17.0;
                break;
            case "eighteen" :
                $val = 18.0;
                break;
            case "nineteen" :
                $val = 19.0;
                break;
            case "twenty"   :
                $val = 20.0;
                break;
        }
    };

fragment
ZERO
    :   '0'
    ;

fragment
NON_ZERO_DIGIT
    :   [1-9]
    ;

fragment
DIGIT
    :   ZERO
    |   NON_ZERO_DIGIT
    ;

INTEGER
    :   NON_ZERO_DIGIT DIGIT*
    ;

DECIMAL
    :   (   INTEGER
        |   ZERO
        )? DOT DIGIT+
    ;

NAME
    :   'one half'
    |   'half'
    |   'one'
    |   'two'
    |   'three'
    |   'four'
    |   'five'
    |   'six'
    |   'seven'
    |   'eight'
    |   'nine'
    |   'ten'
    |   'eleven'
    |   'twelve'
    |   'thirteen'
    |   'fourteen'
    |   'fifteen'
    |   'sixteen'
    |   'seventeen'
    |   'eighteen'
    |   'nineteen'
    |   'twenty'
    ;

VULGAR_FRACTION
    :   '¼'
    |   '½'
    |   '¾'
    |   '⅐'
    |   '⅑'
    |   '⅒'
    |   '⅓'
    |   '⅔'
    |   '⅕'
    |   '⅖'
    |   '⅗'
    |   '⅘'
    |   '⅙'
    |   '⅚'
    |   '⅛'
    |   '⅜'
    |   '⅝'
    |   '⅞'
    ;

AND
    :   'and'
    |   '&'
    ;

DASH
    :   '-'
    ;

DOT
    :   '.'
    ;

SLASH
    :   '/' // normal slash (solidus)
    |   '⁄' // U+2044 : FRACTION SLASH
    ;

WHITESPACE
    :   [ \t\n\r]+ -> skip
    ;

// This is so the lexer can "soak up" anything, and all the error handling
// happens at the parsing layer. Since we're running a boolean distinction for
// recognition, differentiating the layers is irrelevant.
ExtraGarbage
    :   .
    ;