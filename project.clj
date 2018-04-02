(defproject polynomial "0.1.0-SNAPSHOT"
  :description "A clojure representation of polynomials."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/math.combinatorics "0.1.4"]]
  :profiles {:dev {:jvm-opts ["-Dclojure.spec.check-asserts=true"]}}
  :plugins [[lein-marginalia "0.9.1"]]
  :marginalia {:javascript ["https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.3/MathJax.js?config=TeX-MML-AM_CHTML"]}
  :test-selectors {:default {complement :expensive}
                   :expensive :expensive})
