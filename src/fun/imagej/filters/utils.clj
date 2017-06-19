(ns fun.imagej.filters.utils
  (:require [clj-random.core :as rnd]))

(defn make-odd [n] (if (not (odd? n))
                     (+ n 1)
                     n))

(defn random-negate
  [num]
  (let [negate? (rnd/lrand-int 2)]
    (if (= negate? 0)
      num
      (unchecked-negate num))))