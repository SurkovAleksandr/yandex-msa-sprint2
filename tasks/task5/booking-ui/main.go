package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
)

func main() {

	http.HandleFunc("/ping", func(w http.ResponseWriter, r *http.Request) {
		resp, err := http.Get("http://booking-service/ping")
		var otherServiceResponse string
		if err != nil {
			log.Printf("Error calling other-service: %v", err)
			otherServiceResponse = "error contacting other-service"
		} else {
			defer resp.Body.Close()
			body, err := ioutil.ReadAll(resp.Body)
			if err != nil {
				log.Printf("Error reading other-service response: %v", err)
				otherServiceResponse = "error reading other-service response"
			} else {
				otherServiceResponse = string(body)
			}
		}

		fmt.Fprintf(w, "booking-service says: %s", otherServiceResponse)

	})

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
