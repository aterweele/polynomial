(ns polynomial.core-test
  "Tests for the operations on polynomials. Three testing strategies are
  used."
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [polynomial.core :refer :all :as p]))

;;; Generative tests via spec

;; By writing function specs, we can generatively test the opertaions
;; on polynomials.

(s/fdef p/normalize
  :args (s/& ::p/non-canonical-polynomial)
  :ret ::p/polynomial
  :fn (fn [{:keys [args ret]}]
        (every?
          (some-fn
            ;; A term with a coefficient of zero is not present in the
            ;; normalized polynomial.
            #(and (zero? (val %)) (nil? (ret (key %))))
            ;; An term with a non-zero coefficient is present in the
            ;; normalized polynommial.
            #(= (ret (key %)) (val %)))
          args)))

(s/fdef p/poly+
  :args (s/* ::p/polynomial)
  :ret ::p/polynomial)

(s/fdef p/term*
  :args (s/* ::p/term)
  :ret ::p/polynomial)

(s/fdef p/poly*
  :args (s/* ::p/polynomial)
  :ret ::p/polynomial)

;; Cause the function specs to do validation on all calls to
;; polynomial functions.
(-> 'polynomial.core
    stest/enumerate-namespace
    stest/instrument)

;;; Unit tests

;; These show the canonical form of polynomials and the usage of the
;; functions on polynomials.

(deftest examples
  ;; Adapted from
  ;; https://github.com/norvig/paip-lisp/raw/master/PAIP-part2.pdf,
  ;; page 13 of the PDF or page 521 according to page numbers.
  (testing "(x + 1)^10"
    (is (=
          (->> {{} 1 {:x 1} 1}
               repeat
               (take 10)
               (apply poly*))
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
           {}      1}))))

;; Problems I discovered in development, captured as tests.
(deftest regressions
  (testing
      "an exps must be a map. This is to prevent ambiguous polynomials
      like {{} 1 nil 2}."
    (let [one (term*)]
      (is (s/valid? ::p/polynomial one)
        (format "term* did not return a polynomial: %s"
          (s/explain-str ::p/polynomial one))))
    (let [one (poly*)]
      (is (s/valid? ::p/polynomial one)
        (format "poly* did not return a polynomial: %s"
          (s/explain-str ::p/polynomial one))))))

;;; Algebraic properties of polynomials

;; We can turn the algebraic properties of polynomials into generative
;; tests.

;; First, a few helper functions define what commutivity is.

(defn commutative
  "Given a commutative function `f` and a spec for its args, yield a
  property describing `f`'s commutivity, i.e. f(x,y) = f(y,x). `f` is
  the commutative function. `arg-spec` is a spec for x and y."
  [f arg-spec]
  (prop/for-all [x (s/gen arg-spec)
                 y (s/gen arg-spec)]
    (= (f x y)) (f y x)))

(defn commutative-general
  "Given a function `f` of  and a spec for its args, yield a property for
  generalized commutivity. `f` applied to some arguments should be the
  same for any ordering of the arguments."
  [f arg-spec]
  (prop/for-all [args (gen/list (s/gen arg-spec))]
    (gen/let [shuffled-args (gen/shuffle args)]
      (= (apply f args) (apply f shuffled-args)))))

(defspec poly+-commutative
  32
  (commutative poly+ ::p/polynomial))

(defspec poly+-commutative-general
  16
  (commutative-general poly+ ::p/polynomial))

(defspec poly*-commutative
  32
  (commutative poly* ::p/polynomial))

(defspec poly*-commutative-general
  16
  (commutative-general poly* ::p/polynomial))
