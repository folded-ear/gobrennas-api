grammar Number;

start returns [double val]
    :   a=atom { $val = $a.val; }
        (AND b=atom { $val += $b.val; } )*
        EOF
    ;

atom returns [double val]
    :   number { $val = $number.val; }
    |   name { $val = $name.val; }
    |   fraction { $val = $fraction.val; }
    ;

number returns [double val]
    :   integer { $val = $integer.val; }
        (AND? fraction { $val += $fraction.val; } )?
    |   decimal { $val = $decimal.val; }
    ;

integer returns [double val]
    :   INTEGER { $val = Double.parseDouble($INTEGER.text); };

decimal returns [double val]
    :   DECIMAL { $val = Double.parseDouble($DECIMAL.text); }
    ;

fraction returns [double val]
    :   n=integer SLASH d=integer { $val = $n.val / $d.val; }
    |   vf=vulgarFraction { $val = $vf.val; }
    ;

vulgarFraction returns [double val]
    : f=VULGAR_FRACTION {
        String text = $f.text;
        if      ("¼".equals(text)) { $val = 1.0 / 4.0; }
        else if ("½".equals(text)) { $val = 1.0 / 2.0; }
        else if ("¾".equals(text)) { $val = 3.0 / 4.0; }
        else if ("⅐".equals(text)) { $val = 1.0 / 7.0; }
        else if ("⅑".equals(text)) { $val = 1.0 / 9.0; }
        else if ("⅒".equals(text)) { $val = 1.0 / 10.0; }
        else if ("⅓".equals(text)) { $val = 1.0 / 3.0; }
        else if ("⅔".equals(text)) { $val = 2.0 / 3.0; }
        else if ("⅕".equals(text)) { $val = 1.0 / 5.0; }
        else if ("⅖".equals(text)) { $val = 2.0 / 5.0; }
        else if ("⅗".equals(text)) { $val = 3.0 / 5.0; }
        else if ("⅘".equals(text)) { $val = 4.0 / 5.0; }
        else if ("⅙".equals(text)) { $val = 1.0 / 6.0; }
        else if ("⅚".equals(text)) { $val = 5.0 / 6.0; }
        else if ("⅛".equals(text)) { $val = 1.0 / 8.0; }
        else if ("⅜".equals(text)) { $val = 3.0 / 8.0; }
        else if ("⅝".equals(text)) { $val = 5.0 / 8.0; }
        else if ("⅞".equals(text)) { $val = 7.0 / 8.0; }
        else throw new RuntimeException("Unrecognized '" + $f.text + "' vulgar fraction");
    };

name returns [double val]
    :   n=NAME {
        String text = $n.text;
        if      ("one half" .equals(text)) { $val =  0.5; }
        else if ("half"     .equals(text)) { $val =  0.5; }
        else if ("one"      .equals(text)) { $val =  1.0; }
        else if ("two"      .equals(text)) { $val =  2.0; }
        else if ("three"    .equals(text)) { $val =  3.0; }
        else if ("four"     .equals(text)) { $val =  4.0; }
        else if ("five"     .equals(text)) { $val =  5.0; }
        else if ("six"      .equals(text)) { $val =  6.0; }
        else if ("seven"    .equals(text)) { $val =  7.0; }
        else if ("eight"    .equals(text)) { $val =  8.0; }
        else if ("nine"     .equals(text)) { $val =  9.0; }
        else if ("ten"      .equals(text)) { $val = 10.0; }
        else if ("eleven"   .equals(text)) { $val = 11.0; }
        else if ("twelve"   .equals(text)) { $val = 12.0; }
        else if ("thirteen" .equals(text)) { $val = 13.0; }
        else if ("fourteen" .equals(text)) { $val = 14.0; }
        else if ("fifteen"  .equals(text)) { $val = 15.0; }
        else if ("sixteen"  .equals(text)) { $val = 16.0; }
        else if ("seventeen".equals(text)) { $val = 17.0; }
        else if ("eighteen" .equals(text)) { $val = 18.0; }
        else if ("nineteen" .equals(text)) { $val = 19.0; }
        else if ("twenty"   .equals(text)) { $val = 20.0; }
        else throw new RuntimeException("Unrecognized '" + $n.text + "' name");
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
        ) DOT DIGIT+
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
    :   '/'
    ;

WHITESPACE
    :   [ \t\n\r]+ -> skip
    ;
