package main

import (
	"fmt"
	"time"
	"encoding/hex"
	"net/http"
	"io/ioutil"
	cacheclear "github.com/zenground0/aes-clj/aes-go/cacheclear"
)

func aesEnc(hexPtext string) ([]byte, time.Duration) {
	key, err := hex.DecodeString("000102030405060708090a0b0c0d0e0f")
	if err != nil {
		panic(err)
	}

	src, err := hex.DecodeString(hexPtext)
	if err != nil {
		panic(err)
	}

	n := len(key) + 28
	enc, dec  := make([]uint32, n), make([]uint32, n)
	expandKeyGo(key, enc, dec)
	start := time.Now()
	encryptBlockGo(enc, src, src)
	duration := time.Since(start)
	fmt.Printf("%v\n", duration)
	return src, duration
}

func main2() {
	http.HandleFunc("/hex", handleHex)

	fmt.Println("Server is listening on port 8080...")
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		fmt.Println("Error starting server:", err)
	}
	
}


func handleHex(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Invalid request method", http.StatusMethodNotAllowed)
		return
	}

	// Read the body of the request
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		http.Error(w, "Error reading request body", http.StatusInternalServerError)
		return
	}
	defer r.Body.Close()

	// Convert body to string assuming it is in hex format
	cacheclear.ClearL2()
	ptext := string(body)
	ctext, duration := aesEnc(ptext)
	_ = ctext

	// TODO read L2 cache
	// TODO can I actually see if stuff is in the cache
	

	// Write a response
	fmt.Fprintf(w, "\"Elapsed time: %d ns\"\n", duration.Nanoseconds())
}


func main() {
	main2()
}
