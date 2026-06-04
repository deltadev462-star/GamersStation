/**
 * Automated RTL Migration Script
 * 
 * This script migrates CSS files from physical properties to logical properties
 * for RTL/LTR bidirectional support.
 * 
 * Usage: node migrate-rtl.js
 */

const fs = require('fs');
const path = require('path');

// Color output helpers
const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  red: '\x1b[31m',
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

// Migration rules - Physical → Logical property mappings
const migrationRules = [
  // Spacing properties
  {
    pattern: /margin-left:/g,
    replacement: 'margin-inline-start:',
    description: 'margin-left → margin-inline-start'
  },
  {
    pattern: /margin-right:/g,
    replacement: 'margin-inline-end:',
    description: 'margin-right → margin-inline-end'
  },
  {
    pattern: /padding-left:/g,
    replacement: 'padding-inline-start:',
    description: 'padding-left → padding-inline-start'
  },
  {
    pattern: /padding-right:/g,
    replacement: 'padding-inline-end:',
    description: 'padding-right → padding-inline-end'
  },
  
  // Positioning properties (more careful - exclude transforms)
  {
    pattern: /\bleft:\s*(?!50%)/g,
    replacement: 'inset-inline-start: ',
    description: 'left: → inset-inline-start: (excluding 50% for centering)'
  },
  {
    pattern: /\bright:\s*(?!50%)/g,
    replacement: 'inset-inline-end: ',
    description: 'right: → inset-inline-end: (excluding 50% for centering)'
  },
  
  // Text alignment
  {
    pattern: /text-align:\s*left\b/g,
    replacement: 'text-align: start',
    description: 'text-align: left → text-align: start'
  },
  {
    pattern: /text-align:\s*right\b/g,
    replacement: 'text-align: end',
    description: 'text-align: end → text-align: end'
  },
  
  // Border properties
  {
    pattern: /border-left:/g,
    replacement: 'border-inline-start:',
    description: 'border-left: → border-inline-start:'
  },
  {
    pattern: /border-right:/g,
    replacement: 'border-inline-end:',
    description: 'border-right: → border-inline-end:'
  },
];

// Files to migrate (relative to src directory)
const filesToMigrate = [
  'components/Hero/Hero.css',
  'components/SearchOverlay/SearchOverlay.css',
  'components/FormInput/FormInput.css',
  'components/ProductGrid/ProductGrid.css',
  'components/MessagesTab/MessagesTab.css',
  'components/ErrorNotification/ErrorNotification.css',
  'components/SuccessPopup/SuccessPopup.css',
  'components/Loading/Loading.css',
  'components/ScrollToTop/ScrollToTop.css',
  'components/CategoryFilter/CategoryFilter.css',
  'components/OptimizedImage/OptimizedImage.css',
  'pages/ChatPage/ChatPage.css',
  'pages/LoginPage/LoginPage.css',
  'pages/RegisterPage/RegisterPage.css',
  'pages/FAQPage/FAQPage.css',
  'pages/ContactPage/ContactPage.css',
  'pages/ProductDetailsPage/ProductDetailsPage.css',
  'pages/ProfilePage/ProfilePage.css',
  'pages/AllProductsPage/AllProductsPage.css',
  'pages/AddProductPage/AddProductPage.css',
];

// RTL override patterns to remove
const rtlOverridePatterns = [
  // Remove manual RTL overrides that only swap left/right
  /\[dir="rtl"\]\s+[^{]+\{\s*(?:right:\s*auto;?\s*)?(?:left:\s*[^;]+;?\s*)*\}/g,
  /\[dir="ltr"\]\s+[^{]+\{\s*(?:left:\s*auto;?\s*)?(?:right:\s*[^;]+;?\s*)*\}/g,
  /:root\[dir="rtl"\]\s+[^{]+\{\s*(?:right:\s*auto;?\s*)?(?:left:\s*[^;]+;?\s*)*\}/g,
  /:root\[dir="ltr"\]\s+[^{]+\{\s*(?:left:\s*auto;?\s*)?(?:right:\s*[^;]+;?\s*)*\}/g,
];

function migrateFile(filePath) {
  try {
    if (!fs.existsSync(filePath)) {
      log(`⚠️  File not found: ${filePath}`, 'yellow');
      return { skipped: true };
    }

    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    let changeCount = 0;

    // Apply migration rules
    migrationRules.forEach(rule => {
      const matches = content.match(rule.pattern);
      if (matches) {
        content = content.replace(rule.pattern, rule.replacement);
        changeCount += matches.length;
      }
    });

    // Remove RTL override patterns (be conservative)
    // Only remove if they're simple left/right swaps
    // Keep icon flipping and complex logic

    if (content !== originalContent) {
      // Create backup
      fs.writeFileSync(`${filePath}.backup`, originalContent);
      
      // Write migrated content
      fs.writeFileSync(filePath, content);
      
      return { success: true, changes: changeCount };
    }

    return { success: true, changes: 0 };
  } catch (error) {
    return { error: error.message };
  }
}

function main() {
  log('\n🚀 Starting RTL Migration...\n', 'blue');
  
  const srcDir = path.join(__dirname, 'src');
  const results = {
    migrated: [],
    skipped: [],
    errors: [],
  };

  filesToMigrate.forEach(file => {
    const fullPath = path.join(srcDir, file);
    log(`Processing: ${file}`, 'blue');
    
    const result = migrateFile(fullPath);
    
    if (result.skipped) {
      results.skipped.push(file);
      log(`  ⏭️  Skipped (file not found)\n`, 'yellow');
    } else if (result.error) {
      results.errors.push({ file, error: result.error });
      log(`  ❌ Error: ${result.error}\n`, 'red');
    } else if (result.changes > 0) {
      results.migrated.push({ file, changes: result.changes });
      log(`  ✅ Migrated (${result.changes} changes)\n`, 'green');
    } else {
      log(`  ℹ️  No changes needed\n`, 'blue');
    }
  });

  // Summary
  log('\n📊 Migration Summary:\n', 'blue');
  log(`✅ Migrated: ${results.migrated.length} files`, 'green');
  log(`⏭️  Skipped: ${results.skipped.length} files`, 'yellow');
  log(`❌ Errors: ${results.errors.length} files`, 'red');

  if (results.migrated.length > 0) {
    log('\n📝 Migrated Files:', 'green');
    results.migrated.forEach(({ file, changes }) => {
      log(`  - ${file} (${changes} changes)`, 'green');
    });
  }

  if (results.errors.length > 0) {
    log('\n⚠️  Errors:', 'red');
    results.errors.forEach(({ file, error }) => {
      log(`  - ${file}: ${error}`, 'red');
    });
  }

  log('\n💡 Next Steps:', 'blue');
  log('1. Review the migrated files manually', 'blue');
  log('2. Test in both RTL (Arabic) and LTR (English) modes', 'blue');
  log('3. Backup files were created with .backup extension', 'blue');
  log('4. Remove .backup files after verifying migrations\n', 'blue');
}

main();
