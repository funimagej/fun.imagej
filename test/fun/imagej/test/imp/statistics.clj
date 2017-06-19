(ns fun.imagej.test.imp.statistics
  (:use [fun.imagej imp]
        [fun.imagej.imp  statistics]
        [clojure.test]))

(deftest test-empty-image
  (let [imp1 (create-imp :width 10 :height 10)]
    (is (zero? (:mean (get-image-statistics imp1))))))
