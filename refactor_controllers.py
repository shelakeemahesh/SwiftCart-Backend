import os
import re

CONTROLLER_DIR = 'src/main/java/com/swiftcart/controller'

def refactor_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Add import if missing
    if 'com.swiftcart.dto.response.ApiResponse' not in content:
        content = re.sub(r'package com.swiftcart.controller;', 
                         'package com.swiftcart.controller;\n\nimport com.swiftcart.dto.response.ApiResponse;', 
                         content)

    # Replace ResponseEntity<Type> with ResponseEntity<ApiResponse<Type>>
    # Be careful not to replace ResponseEntity<ApiResponse...
    content = re.sub(r'ResponseEntity<(?!ApiResponse)([^>]+)>', r'ResponseEntity<ApiResponse<\1>>', content)

    # Replace ResponseEntity.ok(...) with ResponseEntity.ok(ApiResponse.success(...))
    # Note: this simple regex handles single line ResponseEntity.ok
    # It might need careful balancing.
    # Instead, we can match ResponseEntity.ok( to ResponseEntity.ok(ApiResponse.success(
    # and add a closing paren.
    
    # A safer approach for ResponseEntity.ok(x) -> ResponseEntity.ok(ApiResponse.success(x))
    # We find ResponseEntity.ok( and keep track of parentheses.
    out = []
    i = 0
    while i < len(content):
        if content[i:].startswith('ResponseEntity.ok('):
            out.append('ResponseEntity.ok(ApiResponse.success(')
            i += len('ResponseEntity.ok(')
            
            # Now we need to find the matching closing parenthesis
            paren_count = 1
            start_expr = i
            while i < len(content) and paren_count > 0:
                if content[i] == '(':
                    paren_count += 1
                elif content[i] == ')':
                    paren_count -= 1
                i += 1
            
            # i is now the index right after the closing parenthesis
            expr = content[start_expr:i-1]
            out.append(expr)
            out.append('))')
            continue
            
        elif content[i:].startswith('ResponseEntity.status(HttpStatus.CREATED).body('):
            out.append('ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(')
            i += len('ResponseEntity.status(HttpStatus.CREATED).body(')
            
            paren_count = 1
            start_expr = i
            while i < len(content) and paren_count > 0:
                if content[i] == '(':
                    paren_count += 1
                elif content[i] == ')':
                    paren_count -= 1
                i += 1
                
            expr = content[start_expr:i-1]
            out.append(expr)
            out.append('))')
            continue

        elif content[i:].startswith('ResponseEntity.created('):
            # Complex, maybe skip or handle specifically
            out.append(content[i])
            i += 1
        else:
            out.append(content[i])
            i += 1

    with open(filepath, 'w') as f:
        f.write("".join(out))

for root, _, files in os.walk(CONTROLLER_DIR):
    for f in files:
        if f.endswith('.java'):
            refactor_file(os.path.join(root, f))
