(ns app.core
  (:gen-class)
  (:use markdown.core
        [clojure.string :only (join split)]
        [io.aviso.ansi]
        [io.aviso.logging])
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]))



(defn show-banner []
  (println "EXPERIENCE WALL"))

(defn show-help []
  (show-banner)
  (println (join "\n"
                 ["Usage:"
                  "Ewall [command]"])))

(defn new-ewall
  [rest]
  (fs/copy-dir "temperlate" "bb"))

(defn new-experience
  [rest]
  (println))

(defn start-server
  [rest]
  (println))

(defn release-experience
  [rest]
  (println))

(defn -main
  [& args]
  (if (< (count args) 1)
    (show-help)
    (let [command (first args)]
      (cond
        (= command "init") (new-ewall (rest args))
        (= command "new") (new-experience (rest args))
        (= command "release") (release-experience (rest args))
        (= command "serve") (start-server (rest args))
        (= command "help") (show-help)
            :else (show-help)))))
