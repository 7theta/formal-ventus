(ns formal-ventus.example.views
  (:require [formal-ventus.core :as fv]))

(def sample-schema
  [:map
   [:profile
    {:title "Profile"
     :subtitle "This information will be displayed publicly so be careful what you share."}
    [:map
     [:username {:optional true} :string]
     [:about :string]]]
   [:personal-information
    {:title "Personal Information"
     :subtitle "Use a permanent address where you can receive mail."}
    [:map
     [:first-name :string]
     [:last-name :string]
     [:age {:default-value 30} :int]
     [:email-address :string]
     [:country [:enum :united-states :canada]]
     [:street-address :string]
     [:city :string]
     [:province :string]
     [:postal-code :string]]]])

(defn main-panel
  []
  [:div {:class-name "w-full max-w-md"}
   [fv/form
    {:schema sample-schema
     :on-change #(cljs.pprint/pprint {:sample %})}]])
