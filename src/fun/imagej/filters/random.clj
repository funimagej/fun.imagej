(ns fun.imagej.filters.random
  (:use [fun.imagej imp project utils]
        [fun.imagej.imp calculator roi]
        [fun.imagej.segmentation utils]
        [fun.imagej.filters utils])
  (:require [clj-random.core :as rnd]))


(defn random-filter
  "Returns a random filter and dimensions given the dimension bounds, 
   possible range of element values and the square flag"
  ([] (random-filter 8 2 false))
  ([dim-bounds num-bounds] (random-filter dim-bounds num-bounds false))
  ([dim-bounds num-bounds square?]
    (let [width (make-odd (rnd/lrand-int dim-bounds));should use clj-random 
          height (if (true? square?)
                   width
                   (make-odd (rnd/lrand-int dim-bounds)))
          filter (repeatedly (* width height) #(random-negate (rnd/lrand num-bounds)))]
    [(float-array filter) width height])))