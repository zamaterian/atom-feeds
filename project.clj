(defproject yij/atom-feeds "1.2.9"
  :description "Rest api to create atom entries, and publish atom feeds"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                                  javax.jms/jms
                                                  com.sun.jdmk/jmxtools
                                                  com.sun.jmx/jmxri]]]
  :dev-dependencies
   [[lein-clojars "0.6.0"]
   [lein-lazytest "1.0.1"]]
  :lazytest-path ["src" "test/unit"]
  )
