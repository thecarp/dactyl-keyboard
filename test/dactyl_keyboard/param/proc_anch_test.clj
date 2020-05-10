(ns dactyl-keyboard.param.proc-anch-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.fixture :refer [unit-testing-accessor]]
            [dactyl-keyboard.param.proc.anch :as anch]))


(deftest body-resolution
  (testing "explicit"
    (is (= (anch/resolve-body
             (unit-testing-accessor {})
             :main-body
             :origin)
           :main-body)))
  (testing "key mount"
    (is (= (anch/resolve-body
             (unit-testing-accessor
                {:derived {:anchors {::k {::anch/type ::anch/key-mount}}}})
             :auto
             ::k)
           :main-body)))
  (testing "origin without central housing"
    (let [base {:main-body {:reflect false}
                :central-housing {:include false}
                :derived {:anchors {:origin {::anch/type ::anch/origin}}}}]
      (is (= (anch/resolve-body (unit-testing-accessor base) :auto :origin)
             :main-body))
      (is (= (anch/resolve-body (unit-testing-accessor
                                  (assoc-in base [:main-body :reflect] true))
                                :auto :origin)
             :main-body))
      (is (= (anch/resolve-body (unit-testing-accessor
                                  (assoc-in base [:central-housing :include] true))
                                :auto :origin)
             :main-body))))
  (testing "origin with central housing"
    (is (= (anch/resolve-body
             (unit-testing-accessor
               {:main-body {:reflect true}
                :central-housing {:include :true}
                :derived {:anchors {:origin {::anch/type ::anch/origin}}}})
             :auto
             :origin)
           :central-housing)))
  (testing "central housing gabel"
    (is (= (anch/resolve-body
             (unit-testing-accessor
                {:derived {:anchors {::k {::anch/type ::anch/central-gabel}}}})
             :auto
             ::k)
           :central-housing)))
  (testing "central housing adapter"
    (is (= (anch/resolve-body
             (unit-testing-accessor
                {:derived {:anchors {::k {::anch/type ::anch/central-adapter}}}})
             :auto
             ::k)
           :main-body)))
  (testing "recursion through a number of links"
    (let [base {:derived {:anchors {::m {::anch/type ::anch/key-mount}
                                    ::k {::anch/type ::anch/port-holder
                                         ::anch/primary ::p}
                                    ::p {::anch/type ::anch/port-hole}
                                    ::s {::anch/type ::anch/secondary}
                                        ::anch/primary :origin}}
                :ports {::p {:anchoring {:anchor ::s}}}
                :secondaries {::s {:anchoring {:anchor ::m}}}}
          central (assoc-in base [:derived :anchors ::m ::anch/type] ::anch/central-gabel)
          res-main (partial anch/resolve-body (unit-testing-accessor base) :auto)
          res-central (partial anch/resolve-body (unit-testing-accessor central) :auto)]
      ; Lengthening chains of resolution:
      (is (= (res-main ::m) :main-body))  ; Special treatment.
      (is (= (res-main ::s) :main-body))  ; Defer to m.
      (is (= (res-main ::p) :main-body))  ; Defer to s.
      (is (= (res-main ::k) :main-body))  ; Defer to p.
      ; And once again with a different definition of m:
      (is (= (res-central ::m) :central-housing))
      (is (= (res-central ::s) :central-housing))
      (is (= (res-central ::p) :central-housing))
      (is (= (res-central ::k) :central-housing)))))
