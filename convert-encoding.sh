#!/bin/bash

# Script to convert lesson files from iso-latin-1 to utf-8 encoding
# if they don't already specify UTF-8 encoding

set -e

echo "🔄 Converting lesson files from iso-latin-1 to UTF-8..."

# Process each .txt file in public/lessons
find public/lessons -name "*.txt" -type f | while read -r file; do
    echo "📄 Processing: $file"
    
    # Check if file already has UTF-8 encoding specified
    if grep -q "^#encoding=UTF-8" "$file"; then
        echo "   ✅ Already UTF-8 encoded, skipping"
    else
        # Convert from iso-latin-1 to UTF-8 and add encoding header
        temp_file="${file}.tmp"
        echo "#encoding=UTF-8" > "$temp_file"
        iconv -f iso-8859-1 -t UTF-8 "$file" >> "$temp_file"
        mv "$temp_file" "$file"
        echo "   ✅ Converted from iso-latin-1 to UTF-8"
    fi
done

echo "🎉 Conversion complete!"