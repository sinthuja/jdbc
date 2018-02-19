package ballerina.builtin;

@Description { value:"Releases the database connection."}
@Param { value:"dt: The table object" }
documentation {
Releases the database connection.
- #dt The table object
}
public native function <table dt> close ();

@Description { value:"Checks for a new row in the given table. If a new row is found, moves the cursor to it."}
@Param { value:"dt: The table object" }
@Return { value:"True if there is a new row; false otherwise" }
documentation {
Releases the database connection.
- #dt The table object
- #contains True if there is a new row; false otherwise
}
public native function <table dt> hasNext () (boolean contains);

@Description { value:"Retrives the current row and return a struct with the data in the columns"}
@Param { value:"dt: The table object" }
@Return { value:"The resulting row as a struct" }
documentation {
Checks for a new row in the given table. If a new row is found, moves the cursor to it.
- #dt The table object
- #row The resulting row as a struct
}
public native function <table dt> getNext () (any row);
