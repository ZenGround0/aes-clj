package main

import (
	"fmt"
	//	"time"
	cacheclear "github.com/zenground0/aes-clj/aes-go/cacheclear"
)

// Memory should take ~1 cache line
type Thing struct {
	a int64
	b int64
	c [4]byte
}

func main() {
	// setup thing and warm cache
	thing := new(Thing)
	thing.a, thing.b, thing.c = 5, 600000, [4]byte{0xca, 0xfe, 0xac, 0xe0}
	start := cacheclear.Cputicks()
	copy := *thing
	duration := cacheclear.Cputicks() - start
	copy.a = copy.b + 1
	fmt.Printf("warm up cache: %d\n", duration)

	// measure time to retrieve cached thing
	start = cacheclear.Cputicks()
	quick := *thing
	duration = cacheclear.Cputicks() - start
	quick.a += quick.b
	fmt.Printf("cached mem load: %d\n", duration)

	cacheclear.ClearL1()
	start = cacheclear.Cputicks()	
	slow := *thing
	duration = cacheclear.Cputicks() - start	
	slow.b += slow.a
	fmt.Printf("L1 cache miss mem load: %d\n", duration)

	cacheclear.ClearL2()
	start = cacheclear.Cputicks()	
	slower := *thing
	duration = cacheclear.Cputicks() - start	
	slower.a += slower.b
	fmt.Printf("L2 cache miss mem load: %d\n", duration)
	
	cacheclear.ClearL3()
	start = cacheclear.Cputicks()	
	slowest := *thing
	duration = cacheclear.Cputicks() - start	
	slowest.a += slower.b +6
	fmt.Printf("L3 cache miss mem load: %d\n", duration)

}

