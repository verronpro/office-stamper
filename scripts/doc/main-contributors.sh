# This shell script looks for all Java files in the 'src' directory and its subdirectories
# and then displays the top two contributors for each file.

# Loops through each Java file found in the 'src' directory and its subdirectories.
for java_file in $(find src -type f -name '*.java');
do
  # Uses 'git shortlog -ns' command to list contributors sorted by contribution quantity.
  # The 'head -n 2' command only takes the top 2 contributors.
  # The 'cut -c8-' command removes the initial part of each line that contains the number of commits.
  # The 'tr '\n' ' '' command replaces the newline characters with spaces to format the output as a single line.
  contributors=$(\
    git shortlog -ns "$java_file" \
    | head -n 2                   \
    | cut -c8-                    \
    | tr '\n' ' '                 \
  );

  # Prints the filename and contributors formatted as a line of output.
  printf "%s %s\n" "$java_file" "$contributors";
done
