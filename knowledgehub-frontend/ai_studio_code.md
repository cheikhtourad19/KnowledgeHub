# KnowledgeHub Frontend Development Guidelines

## UI Framework & Styling

### PrimeNG Components

- **ALWAYS use PrimeNG components when possible** for UI elements (buttons, tables, dialogs, forms, etc.)
- Common PrimeNG components to use:
  - `<p-button>` for buttons
  - `<p-table>` for data tables
  - `<p-dialog>` for modals/dialogs
  - `<p-dropdown>`, `<p-multiSelect>` for select inputs
  - `<p-calendar>` for date pickers
  - `<p-inputText>`, `<p-textarea>` for form inputs
  - `<p-card>` for card layouts
  - `<p-panel>` for collapsible sections
  - `<p-menu>`, `<p-menubar>` for menus
  - `<p-paginator>` for pagination
  - And many more available in PrimeNG library

### Tailwind CSS

- **ALWAYS use Tailwind CSS classes** for styling and layout
- Use Tailwind utility classes for spacing, colors, typography, flexbox, grid, etc.
- Prefer Tailwind classes over custom CSS whenever possible
- Example: `class="flex items-center justify-between p-4 bg-white rounded-lg shadow-md"`

## Existing Infrastructure

### Loading State Management

- **A loading interceptor is already implemented** (`loading.interceptor.ts`)
- This interceptor automatically handles loading states for HTTP requests
- No need to manually manage loading spinners for API calls
- The loading overlay component is already integrated and will show automatically during data fetching

### Notifications

- **A `<p-toast>` component is already configured in app.component**
- Use the PrimeNG MessageService to show notifications
- No need to add additional toast components
- Example usage in services/components:

  ```typescript
  constructor(private messageService: MessageService) {}

  this.messageService.add({
    severity: 'success', // 'success' | 'info' | 'warn' | 'error'
    summary: 'Success',
    detail: 'Operation completed successfully'
  });
  ```

## Color Palette

Based on the college branding, use these colors:

### Primary Colors

- **Red:** `#C60C30` (Used for the main banner, active links, and prominent headings)
- **White:** `#FFFFFF` (Used for background, text on red backgrounds, and navigation items)
- **Dark Gray/Black:** `#333333` (Used for main text, icons, and some navigation elements)

### Secondary/Accent Colors

- **Lighter Gray:** `#F0F0F0` (Used for backgrounds of sections, borders, or subtle distinctions)
- **Medium Gray:** `#666666` (Used for secondary text, hover states, or less prominent information)

### Example Usage

- **Main Banner Background:** `#C60C30`
- **Main Text Color:** `#333333`
- **Navigation Links (Default):** `#333333`
- **Navigation Links (Hover/Active):** `#C60C30`
- **Background Sections:** `#FFFFFF` or `#F0F0F0`
- **Button Background:** `#C60C30`
- **Button Text:** `#FFFFFF`

### Tailwind Color Classes

- Use `bg-[#C60C30]` or `text-[#C60C30]` for the primary red color
- Use `bg-gray-100` or `#F0F0F0` for light backgrounds
- Use `text-gray-800` or `#333333` for main text

## Image to illustrate the palette

![Color Palette](color_palette.png)
