# Key-value database
## Project made for programming basics course at Department of Mathematics and Computer Science, St. Petersburg State University

[Task definition](./TASK.md)

### Documentation

This utility is a console interface to a database that supports operations for searching, inserting and deleting text values using their corresponding text keys.

#### Input data

This utility has 2 modes of working with input data.

When run with parameters, it accepts one command, executes it or prints an error message, and exits.

When run without parameters, it accepts as many commands as it likes, 1 per line until the end of input, and responds to them as it is received.

#### Interface:

There are three types of commands:
* *add key value* - adds *value* by *key* to the database, if there is no such key in the database, otherwise it does nothing and displays an error message to the user
* *remove key* - if there is a *key* in the database, then it removes it, otherwise it does nothing and displays an error message to the user
* *get key* - if there is a *key* in the database, then it displays the value corresponding to it, otherwise it does nothing and displays n error message to the user

#### Examples of using:

     $ db add key value
     $db get key

or

     $db
     add key value
     get key

#### General idea

The database is a binary tree, the leaves of which store files corresponding to existing key-value pairs.
The root directory is specified by the constant *STARTDIR*.
For each key, a 32-bit binary hash is considered, which will be the path to the leaf in which the corresponding file will be stored.

For example, if the key hash is *00101001010010101001010110001101*, then the file will be stored in the *STARTDIR/0/0/1/0/1/0/0/1/0/1/0/0/1/0/1/0 directory /1/0/0/1/0/1/0/1/1/0/0/0/1/1/0/1*.

Due to the ollisions, more than one file can be stored in a folder, so when adding a file to a directory, unoccupied file name is selected using MEX.
When searching for a specific key all files in directory correspoding to the hash are checked.

To optimize the number of operations and the memory usage, when adding new key-value pairs, not the full path of 32 levels is created, but only its prefix, such that on other keys in our database have the same hash prefix.
Also, when deleting a key-value pair, directories that are no longer used are deleted.
