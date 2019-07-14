grammar Float;

start returns [float val]
    :   a=atom { $val = $a.val; }
        (AND b=atom { $val += $b.val; } )*
        EOF
    ;

atom returns [float val]
    :   number { $val = $number.val; }
    |   name { $val = $name.val; }
    |   fraction { $val = $fraction.val; }
    ;

number returns [float val]
    :   integer { $val = $integer.val; }
        (AND? fraction { $val += $fraction.val; } )?
    |   decimal { $val = $decimal.val; }
    ;

integer returns [float val]
    :   INTEGER { $val = Float.parseFloat($INTEGER.text); };

decimal returns [float val]
    :   DECIMAL { $val = Float.parseFloat($DECIMAL.text); }
    ;

fraction returns [float val]
    :   n=integer SLASH d=integer { $val = $n.val / $d.val; }
    |   vf=vulgarFraction { $val = $vf.val; }
    ;

vulgarFraction returns [float val]
    : f=VULGAR_FRACTION {
        String text = $f.text;
        if      ("¼".equals(text)) { $val = 1f / 4f; }
        else if ("½".equals(text)) { $val = 1f / 2f; }
        else if ("¾".equals(text)) { $val = 3f / 4f; }
        else if ("⅐".equals(text)) { $val = 1f / 7f; }
        else if ("⅑".equals(text)) { $val = 1f / 9f; }
        else if ("⅒".equals(text)) { $val = 1f / 10f; }
        else if ("⅓".equals(text)) { $val = 1f / 3f; }
        else if ("⅔".equals(text)) { $val = 2f / 3f; }
        else if ("⅕".equals(text)) { $val = 1f / 5f; }
        else if ("⅖".equals(text)) { $val = 2f / 5f; }
        else if ("⅗".equals(text)) { $val = 3f / 5f; }
        else if ("⅘".equals(text)) { $val = 4f / 5f; }
        else if ("⅙".equals(text)) { $val = 1f / 6f; }
        else if ("⅚".equals(text)) { $val = 5f / 6f; }
        else if ("⅛".equals(text)) { $val = 1f / 8f; }
        else if ("⅜".equals(text)) { $val = 3f / 8f; }
        else if ("⅝".equals(text)) { $val = 5f / 8f; }
        else if ("⅞".equals(text)) { $val = 7f / 8f; }
        else throw new RuntimeException("Unrecognized '" + $f.text + "' vulgar fraction");
    };

name returns [float val]
    :   n=NAME {
        String text = $n.text;
        if      ("one half" .equals(text)) { $val =  0.5f; }
        else if ("half"     .equals(text)) { $val =  0.5f; }
        else if ("one"      .equals(text)) { $val =  1f; }
        else if ("two"      .equals(text)) { $val =  2f; }
        else if ("three"    .equals(text)) { $val =  3f; }
        else if ("four"     .equals(text)) { $val =  4f; }
        else if ("five"     .equals(text)) { $val =  5f; }
        else if ("six"      .equals(text)) { $val =  6f; }
        else if ("seven"    .equals(text)) { $val =  7f; }
        else if ("eight"    .equals(text)) { $val =  8f; }
        else if ("nine"     .equals(text)) { $val =  9f; }
        else if ("ten"      .equals(text)) { $val = 10f; }
        else if ("eleven"   .equals(text)) { $val = 11f; }
        else if ("twelve"   .equals(text)) { $val = 12f; }
        else if ("thirteen" .equals(text)) { $val = 13f; }
        else if ("fourteen" .equals(text)) { $val = 14f; }
        else if ("fifteen"  .equals(text)) { $val = 15f; }
        else if ("sixteen"  .equals(text)) { $val = 16f; }
        else if ("seventeen".equals(text)) { $val = 17f; }
        else if ("eighteen" .equals(text)) { $val = 18f; }
        else if ("nineteen" .equals(text)) { $val = 19f; }
        else if ("twenty"   .equals(text)) { $val = 20f; }
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
