# Spark Mart Update Notes

This package updates the existing Spring Boot + Thymeleaf Spark Mart project using the provided Daraz-style reference screenshots.

## Main changes

- Guest/public storefront is now available without forced customer login.
- Customer login/register is still available and required before cart, checkout, payment, profile and order pages.
- Top navigation was redesigned in an orange Daraz-inspired style.
- Public navbar includes customer login/account popup, cart icon, primary navbar search, and Become a Seller option.
- Seller login points to the admin dashboard and has a seller-center inspired login screen.
- Customer account popup contains Manage My Account, My Orders, Wishlist, Reviews, Returns/Cancellations and Logout.
- Seller account popup appears on the public navbar after seller/admin login.
- Navbar search is now the only search entry point and renders results on the public home page instead of a separate visible search page.
- Home page now has image-based category tiles and a Just For You showcase with Load More behavior.
- Product cards were redesigned to a Daraz-style compact card with price, rating and icon-only action buttons.
- Product details page was redesigned with product gallery, rating row, price, promotions, quantity controls, Buy Now, Add to Cart, delivery options and seller card.
- Payment page was redesigned like the supplied reference screenshots with tabbed methods: Card, Nagad, bKash, Cash on Delivery plus disabled Installment/Rocket placeholders.
- Payment confirmation supports Card, Nagad, bKash and Cash on Delivery demo flow.
- Orders now show clear details with product list, customer/delivery info, payment status and order status.
- Customer can update delivery information while order is still Order Sent/Approved.
- Customer can cancel an order while status transition allows cancellation.
- Footer remains: Created by Rejwana Akter.
- Public action buttons were reduced and replaced with icon-style buttons where appropriate.

## Routes to test

- Public home: `/`
- Customer login: `/login`
- Customer register: `/register`
- Seller/admin login: `/admin/login`
- Product details: `/product/{id}`
- Cart: `/cart`
- Checkout: `/checkout`
- Payment: `/payment`
- Customer orders: `/orders`
- Admin dashboard: `/admin/dashboard`

## Demo credentials

Seller/admin:

- Email: `admin@sparkmart.com`
- Password: `admin123`

## Note

The project uses H2 file database with `spring.jpa.hibernate.ddl-auto=update`. New order item image fields will be added automatically when the app starts.

## Refinement update
- Moved Help & Support, Become a Seller, Track Order, and বাংলা / English from the orange top utility bar into the main navbar.
- Removed the duplicate cart button from the navbar. Cart remains available through the floating cart icon/drawer only.
- Removed the Daraz-orange public header/hero from the visible customer storefront and restored the previous Spark Mart theme.
- Restored the original Spark Mart hero section while keeping public/guest browsing, category image grid, Just For You section, Load More, account popups, seller login, product details, and reference-style payment tabs.
- Updated seller login from Daraz seller-center style to the existing Spark Mart login theme.
- Kept icon-based primary actions and reduced duplicate text/action buttons.

## Final Professional Update

- Customer registration now stores phone, delivery address, district/area and default order note.
- Checkout now pre-fills saved customer delivery details and saves edited checkout details back to the profile to avoid repeated input.
- Added seller account creation at `/seller/register`.
- Secure role design: every seller is also a customer account; only accounts with SELLER/ADMIN role can enter Seller Center.
- Existing customers can upgrade to seller using the same email/password.
- Seller login now supports real seller accounts plus the default admin account.
- Customer login auto-enables Seller Center session if the logged-in account is already a seller.
- Added Font Awesome icon library and replaced most emoji/text-only action buttons with professional icons plus text where suitable.
- Refined customer/seller account popovers, checkout consent alignment, profile layout, product cards, product details actions, payment buttons, and admin sidebar/buttons.
- Admin controls remain responsive and cleaner for product/category/order actions.
