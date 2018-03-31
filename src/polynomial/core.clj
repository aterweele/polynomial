(ns polynomial.core
  "Operations on a representation of polynomials. Base on Paradigms of
  Artificial Intelligence Programming chapter 15."
  (:require [clojure.math.combinatorics :as combo]
            [clojure.spec.alpha :as s]))

;; the form of variables to powers, e.g. (x^a)(y^b)(z^c)
(s/def ::exps (s/map-of any? (s/and integer? pos?)))

;; A term is an exps, representing the variables of a term and their
;; powers, and a non-zero coefficient.
(s/def ::term
  (s/and
    ;; Commented out, as map entries do not have a generator.
    #_map-entry?
    (s/tuple ::exps (s/and number? (complement zero?)))))

;; operations like 1 - 1 yield zero (represented as something like 0 *
;; x^0), but we do not represent zero as a coefficient. This will be
;; the argument to `normalize`.
(s/def ::non-canonical-polynomial (s/nilable (s/map-of ::exps number?)))

;; A polynomial is a map of terms.
(s/def ::polynomial
  (s/and
    ::non-canonical-polynomial
    (s/nilable (s/map-of ::exps (complement zero?)))))

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

