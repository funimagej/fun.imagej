(ns fun.imagej.test.n5
  (:require [clojure.test :refer :all]
            [fun.imagej.fu.n5 :as n5]
            [fun.imagej.core :as ij]
            [fun.imagej.img :as img]
            [fun.imagej.img.shape :as shape]))

(def n5-cache (n5/open-n5-cache (str (System/getProperty "user.dir")
                                     java.io.File/separator
                                     "cache-test.n5")))

(def mem #(n5/memoize-with-n5-cache % n5-cache))

(defn im-creator
  "Make a simple empty small image"
  []
  (fun.imagej.ops.create/img (net.imglib2.FinalInterval. (long-array [10 10 10]))
                             (net.imglib2.type.numeric.real.DoubleType.)))

(defn update-im
  "Add some value to an image"
  [im]
  (img/set-val im (long-array [2 2 2]) (double 17)))

(def im ((mem update-im) ((mem im-creator))))

(deftest n5-attributes
  (pos? (count (slurp (str (System/getProperty "user.dir")
                     java.io.File/separator
                     "cache-test.n5"
                     java.io.File/separator
                     "attributes.json")))))
