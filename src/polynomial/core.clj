(ns polynomial.core
  "Operations on a representation of polynomials. Based on Paradigms of
  Artificial Intelligence Programming chapter 15."
  (:require [clojure.math.combinatorics :as combo]
            [clojure.spec.alpha :as s]))

;; the form of variables to powers, e.g. \\(x^iy^jz^k\\)
(s/def ::exps (s/map-of #_any? keyword? (s/and integer? pos?)))

;; A term is an `::exps`, representing the variables of a term and
;; their powers, and a coefficient. In any polynomial, there are an
;; infinite number of terms which have a coefficient of zero. Thus,
;; the coefficient must be non-zero.
(s/def ::term
  (s/and
    ;; Commented out, as map entries do not have a generator.
    #_map-entry?
    (s/tuple ::exps (s/and rational? (complement zero?)))))

;; operations like \\(1 - 1\\) or \\(x - x\\) yield zero, which is
;; represented as something like \\(0x^0\\). However, we do not
;; represent zero as a coefficient. `::non-canonical-polynomial` will
;; be the argument to `normalize`, which will yield the canonical
;; representation of the polynomial.
(s/def ::non-canonical-polynomial (s/nilable (s/map-of ::exps rational?)))

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

