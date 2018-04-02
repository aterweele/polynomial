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

;; If two terms have the same variables to the same powers, then in
;; our representation of the polynomial, they will have the same key
;; in the polynomial map. This is why we `merge-with` -- it takes
;; advantage of the key collision and adds the coefficients together,
;; producing the new term. `+'` is used for arbitrary precision.
(defn poly+
  "Add the given polynomials."
  [& polys]
  (->> polys
       (apply merge-with +')
       normalize))

;; Multiplying terms yields a polynomial of one term. We find the
;; variables and their exponents by merging and adding, because
;; \\(x^ix^j = x^{i+j}\\). We find the coefficient by multiplying all
;; the given coefficients together.
(defn term*
  "Multiply the given terms."
  [& terms]
  {(->> terms
        (map first)
        (apply merge-with +' {}))
   (->> terms
        (map second)
        (reduce *'))})

;; A common way to find the product of two polynomials on paper is to
;; draw a table with the terms of one on the horizontal and the terms
;; of the other on the vertical, then filling in each cell with the
;; product of the two terms. We generalize this to an arbitrary number
;; of polynomials by finding the Cartesian product of `polys`. Given
;; \\(n\\) polynomials, we can think of this as drawing an
;; \\(n\\)-dimensional matrix. Then, we do cellwise multiplication
;; with `term*`. We add the resulting polynomials together with
;; `poly+`.
(defn poly*
  "Multiply the given polynomials."
  [& polys]
  (->> polys
       (apply combo/cartesian-product)
       (map (partial apply term*))
       (reduce poly+)
       normalize))

(defn degree
  [polynomial v]
  (->> polynomial
       (map (comp v first))
       (apply max 0)))

(defn derive-term
  "Find the derivative of `term` with respect to variable `v`"
  [v [exps coefficient :as term]]
  (when-let [exp (exps v)]
    [(assoc exps v (dec exp)) (*' coefficient exp)]))

(defn derivative
  "Find the derivative of `polynomial` with respect to variable `v`."
  [polynomial v]
  (into {} (map (partial derive-term v)) polynomial))
