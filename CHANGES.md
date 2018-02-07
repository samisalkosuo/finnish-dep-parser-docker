### v0.21

- Script omorfi_pos_modified.py combines cut_and_sort.py and omorfi_pos.py.
- Updated Dockerfile. Changed ADD to COPY and other changes.
- Warn unrecognized words only once in finwordnet-servlet.

### v0.20

- Refactoring.
- Replaced conv_u_09.py --output=u with Java code.
- Removed conv_u_09.py --output=09 since input is already 09.
- Removed custom logger from ported code and added SLF4J.
- Combined my_parser_wrapper.sh and tag.sh scripts.
- Moved scripts from server-directory to scripts-directory.
- Added parse_file.sh and parse_text.sh scripts.
- Changed scripts-directory structure.
- Added scripts to build and run local findep-parser image.
- Changed system out log format. Added "-" after timestamp.

### v0.19

- Replaced "cut -f 2 | sort -u" with "cut_and_sort.py" in tag.sh.
- Added SLF4J-logging.
- Refactored, changed package names.
- Added caching to finwordnet-servlet.
- Added hypernymsenses-function to finwordnet-servlet.
- Added this CHANGES file (finally!:-).