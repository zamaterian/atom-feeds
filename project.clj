(defproject yij/atom-feeds "1.0.9"
  :description "Rest api to create atom entries, and publish atom feeds"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                                  javax.jms/jms
                                                  com.sun.jdmk/jmxtools
                                                  com.sun.jmx/jmxri]]]
  :dev-dependencies
  [[uk.org.alienscience/leiningen-war "0.0.9"]
   [org.apache.derby/derby "10.6.1.0"]
   [ring/ring-jetty-adapter "0.3.3"]
   [org.clojars.autre/lein-vimclojure "1.0.0"]
   [compojure "0.5.1"]
   [ring/ring-core "0.3.3" :xclusions [javax.servlet/servlet-api]]
   [ring/ring-servlet "0.3.3" :exclusions [javax.servlet/servlet-api]]
   [ring-json-params "0.1.3"]
   [clj-json "0.3.1"]
   [ring-common "1.0.7"]
   [ring/ring-devel "0.3.3"]
   [autodoc "0.7.1" :exclusions [ant org.clojure/clojure-contrib]]
   [com.stuartsierra/lazytest "1.1.2"]
   [lein-clojars "0.6.0"]
   [lein-lazytest "1.0.1"]
   [javax.servlet/servlet-api "2.5"]]
  :repl-init-script "init.clj" ; init.cjl  is not under version control. se sample-init.clj
  :lazytest-path ["src" "test/unit"]
  :autodoc {
        :name "Atom Feeds"
        :description "Create atom entries, and publish atom feeds"
        :output-path "public/autodoc"}
  )
