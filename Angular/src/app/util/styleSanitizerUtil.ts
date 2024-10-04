import { SecurityContext } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

export class StyleSanitizerUtil {
  static sanitizeContentAndReapplyStyles(content: string, sanitizer: DomSanitizer): string | null {
    // 1. Parse the original HTML content and extract the styles
    const parser = new DOMParser();
    const originalDoc = parser.parseFromString(content, 'text/html');
    const styleMap: string[] = []; // A list to hold the original styles by element order

    // Extract all elements with style and store the styles in the order they appear
    originalDoc.querySelectorAll('[style]').forEach((element) => {
      styleMap.push(element.getAttribute('style') || ''); // Store the style
    });

    // 2. Sanitize the content (this will remove the styles)
    const sanitizedContent = sanitizer.sanitize(SecurityContext.HTML, content);
    if (!sanitizedContent) {
      return null; // If the content is completely removed by sanitization, return null
    }

    // 3. Parse the sanitized content
    const sanitizedDoc = parser.parseFromString(sanitizedContent, 'text/html');

    // 4. Reapply the styles by matching elements based on their order in the DOM tree
    let styleIndex = 0;
    const originalElementsWithStyle = originalDoc.querySelectorAll('[style]');
    const sanitizedElements = sanitizedDoc.querySelectorAll('*'); // Select all elements

    sanitizedElements.forEach((sanitizedElement, index) => {
      // Skip elements that don't have a counterpart with a style in the original document
      if (originalElementsWithStyle[styleIndex]) {
        // Check if the tag name matches between sanitized and original content
        if (sanitizedElement.tagName === originalElementsWithStyle[styleIndex].tagName) {
          // Reapply the original style to the sanitized element
          sanitizedElement.setAttribute('style', styleMap[styleIndex]);
          styleIndex++;
        }
      }
    });

    // 5. Return the final sanitized HTML with the styles reapplied
    return sanitizedDoc.body.innerHTML;
  }
}
