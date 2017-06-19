(ns fun.imagej.test.imp.display
  (:use [fun.imagej imp]
        [clojure.test]))

; Disabling for Travis
#_(deftest test-show-imp
   (let [imp1 (open-imp "http://mirror.imagej.net/images/blobs.gif")]
     (show-imp imp1)
     (is imp1)))
        
