(ns polynomial.core
  "Operations on a representation of polynomials. Based on Paradigms of
  Artificial Intelligence Programming chapter 15."
  (:require [clojure.math.combinatorics :as combo]
            [clojure.spec.alpha :as s]))

;; Here is a polynomial: \\[3x^2y + 2xy^2 + x + 1\\]

;; A polynomial is a sum of _terms_. This one has four: \\(3x^2y\\),
;; \\(2xy^2\\), \\(x\\), and \\(1\\).

;; Each term has a _coefficient_. Notice that all the coefficients are
;; nonzero. Each term has _variables_ raised to some power. In the
;; term \\(x\\), the variable \\(x\\) is raised to the first power and
;; \\(y\\) is raised to the zeroth power. So, we could write the term
;; like \\(x^1y^0\\).

;; Now we introduce our representation of polynomials.

;; The representation of a term is a pair. The first item in the pair
;; is an `::exps` which represents variables and their powers. The
;; second item is the coefficient. We require the coefficient to be
;; non-zero, as discussed.
(s/def ::term
  (s/and
    ;; Commented out, as map entries do not have a generator.
    #_map-entry?
    (s/tuple ::exps (s/and rational? (complement zero?)))))

;; `::exps` represents the form of variables to powers,
;; e.g. \\(x^iy^jz^k\\). The exponents in a polynomial must be
;; positive integers. The representation is a map from variables to
;; their power.
(s/def ::exps (s/map-of #_any? keyword? (s/and integer? pos?)))

;; With this, we can now represent the example polynomial:
#_
{{:x 2 :y 1} 3
 {:x 1 :y 2} 2
 {:x 1}      1
 {}          1}

;; The canonical representation of a polynomial is a map of
;; terms. Here, we are refining the definition of
;; `::non-canonical-polynomial`.
(s/def ::polynomial
  (s/and
    ::non-canonical-polynomial
    (s/nilable (s/map-of ::exps (complement zero?)))))

;; Operations like \\(1 - 1\\) or \\(x - x\\) yield zero, which is
;; represented as `{{} 0}`. However, we do not represent zero as a
;; coefficient. `::non-canonical-polynomial` represents these
;; ill-formed polynomials and will be the argument to `normalize`,
;; which will yield the canonical representation of the polynomial.
(s/def ::non-canonical-polynomial (s/nilable (s/map-of ::exps rational?)))

(defn normalize
  "Remove terms with a coefficient of 0 from `p`."
  [p]
  (into {} (remove (comp zero? second)) p))

(defn poly+
  "Add the given polynomials."
  [& polys]
  (->> polys
       (apply merge-with +')
       normalize))

(defn term*
  "Multiply the given terms."
  [& terms]
  {(->> terms
        (map first)
        (apply merge-with +' {}))
   (->> terms
        (map second)
        (reduce *'))})

(defn poly*
  "Multiply the given polynomials."
  [& polys]
  (->> polys
       (apply combo/cartesian-product)
       (map (partial apply term*))
       (reduce poly+)
       normalize))

