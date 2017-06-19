(ns fun.imagej.project
  (:require [clojure.string :as string]
            [fun.imagej.imp :as imp])
  (:import [ij IJ ImagePlus]
           [java.awt Button]
           [java.awt.event ActionListener]
           [java.io File]))

(defonce params 
	(atom {}))

(defn remove-file-extension
  "Remove a file extension from a string."
  [filename]
  (.substring filename 0 (.lastIndexOf filename ".")))

(defn project-directory 
	[]
	(str (:parent-directory @params) "/" (:basename @params)))

(defn make-project-from-imp
  "Make a project from an ImagePlus."
  ([im]  
    (make-project-from-imp im (str (.directory (.getOriginalFileInfo im)))))
  ([im parent-directory]
	  (swap! params assoc 
	         :basename (remove-file-extension (.getTitle im))
	         :parent-directory parent-directory)
	  (println (str "Project directory: " (project-directory)))
	  (let [dir-file (File. (project-directory))]
		  (when-not (.exists dir-file)
	     (println "Making directory")
	     (.mkdirs dir-file)))))

(defn setup-project-from-filename
  "Make a project from an ImagePlus."
  [filename]
  (let [f (java.io.File. filename)]  
    (swap! params assoc 
	          :basename (remove-file-extension (.getName f))
	          :parent-directory (.getParent f))
	  (println (str "Project directory: " (project-directory)))
	  (let [dir-file (File. (project-directory))]
		  (when-not (.exists dir-file)
	     (println "Making directory")
	     (.mkdirs dir-file)))))

(defn open-imp-project
  "Open imp and setup project variables."
  [filename]
  (let [im (imp/open-imp filename)]
    (make-project-from-imp im)
    im))
