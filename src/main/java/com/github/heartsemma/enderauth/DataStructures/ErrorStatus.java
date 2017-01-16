package com.github.heartsemma.enderauth.DataStructures;


//An enum used to designate how parts of the program went.
public enum ErrorStatus {
	SUCCESS, //Function/Method/Algorithm/Code Snippet went positively without error.
	MIXED_FAILURE, //Parts of ... failed but parts did not. The parts that failed were not critical to the success of .... 
	UNKNOWN, //...may have been either successful or unsuccessful.
	CRITICAL_FAILURE //...failed and we must shut down the plugin as ... was a critical part of EnderAuth and EnderAuth cannot proceed without its success.
}

