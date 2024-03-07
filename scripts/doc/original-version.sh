# Recursive search in the 'src' directory for files (-type f) having '.java' extension
find src -type f -name '*.java' |
# Read each file in the returned list of '.java' files one by one
while read -r java_file
do
  # Get the commit hash of the first commit where this file was introduced (using --follow to track renames).
  # git log provides commit details, --follow flag helps with tracking file renames, --format=%h returns the commit hash
  orig_commit_hash=$(git log --follow --format=%h "$java_file" | tail -n 1)

  #  Using 'git show' to display the contents of 'pom.xml' from that commit.
  #  Use xmlstarlet to extract the project version from the 'pom.xml' file.
  #  xmlstarlet sel -t -v //_:project/_:version --nl : Selects the project version text from the XML, --nl adds a new line after each value
  orig_project_version=$(git show "$orig_commit_hash:pom.xml" | xmlstarlet sel -t -v //_:project/_:version --nl)

  # Print filename and its associated project version from the moment the file was first committed
  echo "$java_file" "$orig_project_version"
done
