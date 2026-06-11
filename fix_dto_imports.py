import os
import re

for root, _, files in os.walk('src/main/java'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            # Replace wildcard imports
            content = content.replace('import com.swiftcart.dto.*;', 'import com.swiftcart.dto.request.*;\nimport com.swiftcart.dto.response.*;')
            
            # Replace specific imports
            def replacer(match):
                class_name = match.group(1)
                if class_name.endswith('Request'):
                    return f'import com.swiftcart.dto.request.{class_name};'
                elif class_name.endswith('Response') or class_name.endswith('DTO'):
                    return f'import com.swiftcart.dto.response.{class_name};'
                elif class_name == 'OrderTrackingDTO.TimelineStepDTO':
                    return 'import com.swiftcart.dto.response.OrderTrackingDTO.TimelineStepDTO;'
                elif class_name == 'OrderTrackingDTO.TrackingItemDTO':
                    return 'import com.swiftcart.dto.response.OrderTrackingDTO.TrackingItemDTO;'
                else:
                    return match.group(0) # don't change
            
            content = re.sub(r'import com.swiftcart.dto\.([A-Za-z0-9_.]+);', replacer, content)
            
            with open(filepath, 'w') as f:
                f.write(content)
