(defproject fun.imagej/fun.imagej "0.4.2-SNAPSHOT"
  :description "Functional Image Processing with ImageJ/FIJI"
  :url "https://github.com/funimagej/fun.imagel"
  :license {:name "Apache v2.0"
            :url "https://github.com/funimagej/fun.imagej/LICENSE"}
  :pom-addition ([:developers [:developer
                               [:id "kephale"]
                               [:name "Kyle Harrington"]
                               [:roles [:role "founder"]
                                [:role "lead"]
                                [:role "debugger"]
                                [:role "reviewer"]
                                [:role "support"]
                                [:role "maintainer"]]]]
                 [:contributors [:contributor
                                 [:name "Curtis Rueden"]
                                 [:properties [:id "ctrueden"]]]])
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [seesaw "1.4.4"]
                 [clj-random "0.1.8"]

                 [com.kephale/random-forests-clj "8278de0"]

                 ; Java libs
                 ;[net.imglib2/imglib2 "b33186a" :exclusions [com.github.jnr/jffi]]
                 [net.imglib2/imglib2 "5.12.0" :exclusions [com.github.jnr/jffi]]
                 [net.imglib2/imglib2-algorithm "0.12.1"]
                 [net.imglib2/imglib2-ij "2.0.0-beta-46"]
                 [net.imglib2/imglib2-cache "1.0.0-beta-16"]
                 [net.imglib2/imglib2-roi "0.12.1"]
                 [net.imagej/imagej "2.5.0" :exclusions [com.github.jnr/jffi
                                                         com.github.jnr/jnr-x86asm
                                                         org.scijava/scripting-renjin]]
                 [net.imagej/imagej-legacy "0.38.1"]
                 
                 [org.json/json "20201115"]
                 
                 [ome/formats-bsd "6.9.1"]
                 [ome/formats-gpl "6.9.1"]
                 [ome/formats-api "6.9.1"]
                 [ome/bio-formats_plugins "6.9.1"]
                 
                 [org.joml/joml "1.10.2"]
                 [graphics.scenery/scenery "cf297d4"]
                 [sc.iview/sciview "6cc9938" :exclusions [com.github.jnr/jffi
                                                          com.github.jnr/jnr-x86asm
                                                          ch.qos.logback/logback-classic
                                                          org.scijava/scripting-renjin]]

                 [net.imagej/imagej-ops "0.46.1" :exclusions [org.joml/joml]]
                 [net.imagej/imagej-mesh "0.8.1"]
                 [net.imagej/imagej-mesh-io "0.1.2"]

                 ;[com.github.saalfeldlab/n5 "a3f0406"]
                 ;[com.github.saalfeldlab/n5-ij "a5517c8"]
                 ;[com.github.saalfeldlab/n5-imglib2 "2a211a3"]

                 [org.janelia.saalfeldlab/n5 "2.5.1"]
                 ;[com.github.saalfeldlab/n5-ij ""]
                 ;[org.org.janelia.saalfeldlab/n5-ij "0.0.2-SNAPSHOT"]
                 [org.janelia.saalfeldlab/n5-imglib2 "4.3.0"]

                 [org.ojalgo/ojalgo "48.1.0"]

                 [sc.fiji/Auto_Threshold "1.17.1"]

                 [org.scijava/scijava-common "2.85.0"]

                 [ch.qos.logback/logback-classic "1.2.3"]

                 [org.morphonets/SNT "2d8af5b6a0"]]


  :resource-paths ["src/main/resource"]
  :java-source-paths ["java"]
  :repositories [["scijava.public" "https://maven.scijava.org/content/groups/public"]
                 ["jitpack.io" "https://jitpack.io"]
                 ["saalfeld-lab-maven-repo" "https://saalfeldlab.github.io/maven"]]
  :deploy-repositories [["releases" {:url "https://maven.imagej.net/content/repositories/releases"
                                     ;; Select a GPG private key to use for
                                     ;; signing. (See "How to specify a user
                                     ;; ID" in GPG's manual.) GPG will
                                     ;; otherwise pick the first private key
                                     ;; it finds in your keyring.
                                     ;; Currently only works in :deploy-repositories
                                     ;; or as a top-level (global) setting.
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]
                        ["snapshots" {:url "https://maven.imagej.net/content/repositories/snapshots"
                                      :username :env/CI_DEPLOY_USERNAME
                                      :password :env/CI_DEPLOY_PASSWORD
                                      :sign-releases false}]]
  ; Try to use lein parent when we can
  :plugins [[lein-exec "0.3.7"]]
  :jvm-opts ["-Xmx32g" "-server" "-Dscenery.Renderer=OpenGLRenderer"])
