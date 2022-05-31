#_(ns fun.imagej.test.snt
  (:import [sc.fiji.snt.annotation AllenUtils])
  (:require [clojure.test :refer :all]))

#_(deftest allen-compartment
  (not
   (nil?
    (AllenUtils/getCompartment "Cerebral cortex"))))
