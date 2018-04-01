(ns ^:expensive polynomial.core-test.expensive
  "Tests that are too expensive to run by default."
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]))

;; For every polynomial function, use its function specification to
;; generate its input, call the function, and check the output with
;; the spec.
(deftest generative
  (doseq [f (stest/enumerate-namespace 'polynomial.core)]
    (testing f
      (let [problems (stest/check f)]
        (is
          #_(empty? problems)
          ;; Do not do the above, as it will cause the test runner to
          ;; print out the unshrunken data structure, which is useless
          ;; and huge.
          (= 0 (count problems))
          (format "fdef for %s failed :\n%s"
            f
            (with-out-str (stest/summarize-results problems))))))))

