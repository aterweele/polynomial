# polynomial

A Clojure representation of polynomials.

You can read through `src` and `test` directly or read it as HTML by
using [Marginalia](https://github.com/gdeer81/marginalia):

```shellsession
$ lein marg src test
```

## Usage

Here is (x + 1)^10. This example is taken from
[PAIP](https://github.com/norvig/paip-lisp/raw/master/PAIP-part2.pdf),
page 13 of the PDF or page 521 according to page numbers.

```clojure
(->> {{} 1 {:x 1} 1}
     repeat
     (take 10)
     (apply poly*))
=>
{{:x 10} 1
 {:x 9}  10
 {:x 8}  45
 {:x 7}  120
 {:x 6}  210
 {:x 5}  252
 {:x 4}  210
 {:x 3}  120
 {:x 2}  45
 {:x 1}  10
 {}      1}
```

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
