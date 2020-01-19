grammar Number;

@header {
    import com.brennaswitzer.cookbook.util.NumberUtils;
}

start returns [double val]
    :   a=atom { $val = $a.val; }
        (AND b=atom { $val += $b.val; } )*
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
        if (!NumberUtils.NAMES.containsKey(text)) {
            throw new RuntimeException("Unrecognized '" + $f.text + "' vulgar fraction");
        }
        $val = NumberUtils.NAMES.get(text);
    };

name returns [double val]
    :   n=NAME {
        String text = $n.text;
        if (!NumberUtils.NAMES.containsKey(text)) {
            throw new RuntimeException("Unrecognized '" + $n.text + "' name");
        }
        $val = NumberUtils.NAMES.get(text);
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
    :   '/' // normal slash (solidus)
    |   '⁄' // U+2044 : FRACTION SLASH
    ;

WHITESPACE
    :   [ \t\n\r]+ -> skip
    ;

// This is so the lexer can "soak up" anything, and all the error handling
// happens at the parsing layer. Since we're runnning a boolean distinction for
// recognition, differentiating the layers is irrelevant.
ExtraGarbage
    :   .
    ;