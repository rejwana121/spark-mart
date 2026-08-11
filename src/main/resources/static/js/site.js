const openButtons = document.querySelectorAll("[data-cart-toggle]");
const closeButtons = document.querySelectorAll("[data-cart-close]");
const root = document.documentElement;

openButtons.forEach((button) => {
  button.addEventListener("click", () => {
    root.classList.add("cart-open");
  });
});

closeButtons.forEach((button) => {
  button.addEventListener("click", () => {
    root.classList.remove("cart-open");
  });
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    root.classList.remove("cart-open");
    document.querySelector("[data-mobile-panel]")?.classList.remove("open");
  }
});

document.querySelectorAll("[data-menu]").forEach((button) => {
  button.addEventListener("click", () => {
    document.querySelector("[data-mobile-panel]")?.classList.toggle("open");
  });
});

document.querySelectorAll("[data-slider]").forEach((slider) => {
  const slides = Array.from(slider.querySelectorAll(".fresh-slide"));
  const dots = Array.from(slider.querySelectorAll("[data-slider-dot]"));
  const prev = slider.querySelector("[data-slider-prev]");
  const next = slider.querySelector("[data-slider-next]");
  let index = slides.findIndex((slide) => slide.classList.contains("is-active"));
  let timer;

  if (slides.length <= 1) {
    slider.classList.add("is-static");
    return;
  }

  index = index < 0 ? 0 : index;

  const show = (nextIndex) => {
    index = (nextIndex + slides.length) % slides.length;
    slides.forEach((slide, slideIndex) => {
      slide.classList.toggle("is-active", slideIndex === index);
    });
    dots.forEach((dot, dotIndex) => {
      dot.classList.toggle("is-active", dotIndex === index);
      dot.setAttribute("aria-current", dotIndex === index ? "true" : "false");
    });
  };

  const stop = () => window.clearInterval(timer);
  const start = () => {
    stop();
    timer = window.setInterval(() => show(index + 1), 4200);
  };

  prev?.addEventListener("click", () => {
    show(index - 1);
    start();
  });

  next?.addEventListener("click", () => {
    show(index + 1);
    start();
  });

  dots.forEach((dot, dotIndex) => {
    dot.addEventListener("click", () => {
      show(dotIndex);
      start();
    });
  });

  slider.addEventListener("mouseenter", stop);
  slider.addEventListener("mouseleave", start);
  slider.addEventListener("focusin", stop);
  slider.addEventListener("focusout", start);

  show(index);
  start();
});

document.querySelectorAll(".category-product-row").forEach((row) => {
  const track = row.querySelector("[data-row-track]");
  const prev = row.querySelector("[data-row-prev]");
  const next = row.querySelector("[data-row-next]");

  if (!track) {
    return;
  }

  const scrollByPage = (direction) => {
    const card = track.querySelector(".product-card");
    const distance = card ? card.getBoundingClientRect().width + 20 : track.clientWidth * 0.8;
    track.scrollBy({ left: direction * distance, behavior: "smooth" });
  };

  prev?.addEventListener("click", () => scrollByPage(-1));
  next?.addEventListener("click", () => scrollByPage(1));
});

// Daraz-inspired Just For You load-more behavior.
document.querySelectorAll("[data-load-more]").forEach((button) => {
  button.addEventListener("click", () => {
    const grid = document.querySelector("[data-load-grid]");
    if (!grid) return;
    const hiddenCards = Array.from(grid.querySelectorAll(".load-more-card.is-hidden")).slice(0, 24);
    hiddenCards.forEach((card) => card.classList.remove("is-hidden"));
    if (!grid.querySelector(".load-more-card.is-hidden")) {
      button.closest(".load-more-wrap")?.remove();
    }
  });
});

// Product details quantity sync for Buy Now and Cart icon buttons.
document.querySelectorAll("[data-qty-master]").forEach((input) => {
  const sync = () => {
    const min = Number(input.getAttribute("min") || 1);
    const max = Number(input.getAttribute("max") || 9999);
    let value = Number(input.value || min);
    value = Math.min(Math.max(value, min), max || value);
    input.value = value;
    document.querySelectorAll("[data-qty-target]").forEach((target) => (target.value = value));
  };
  input.addEventListener("input", sync);
  document.querySelectorAll("[data-qty-minus]").forEach((btn) => btn.addEventListener("click", () => {
    input.value = Math.max(Number(input.getAttribute("min") || 1), Number(input.value || 1) - 1);
    sync();
  }));
  document.querySelectorAll("[data-qty-plus]").forEach((btn) => btn.addEventListener("click", () => {
    const max = Number(input.getAttribute("max") || 9999);
    input.value = Math.min(max, Number(input.value || 1) + 1);
    sync();
  }));
  sync();
});

// Payment method tabs: show one method body and disable hidden inputs so Spring receives clean fields.
document.querySelectorAll("[data-payment-form]").forEach((form) => {
  const tabs = Array.from(form.querySelectorAll("[data-payment-tab]"));
  const sections = Array.from(form.querySelectorAll("[data-payment-section]"));
  const cashFee = document.querySelector("[data-cash-fee]");

  const setMethod = (method) => {
    tabs.forEach((tab) => tab.classList.toggle("is-active", tab.getAttribute("data-payment-tab") === method));
    sections.forEach((section) => {
      const active = section.getAttribute("data-payment-section") === method;
      section.classList.toggle("is-hidden", !active);
      section.querySelectorAll("input, textarea, select").forEach((field) => {
        if (field.type !== "radio" && field.type !== "checkbox") field.disabled = !active;
        if (field.type === "checkbox" && section.hasAttribute("data-payment-section")) field.disabled = !active;
      });
    });
    if (cashFee) cashFee.classList.toggle("is-visible", method === "cod");
  };

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      const radio = tab.querySelector('input[type="radio"]');
      radio.checked = true;
      setMethod(tab.getAttribute("data-payment-tab"));
    });
  });
  setMethod(form.querySelector('input[name="paymentMethod"]:checked')?.value || "card");
});

// Clickable account popovers. Hover still works, but click keeps the menu open for real interaction.
document.querySelectorAll("[data-account-trigger]").forEach((trigger) => {
  trigger.addEventListener("click", (event) => {
    event.preventDefault();
    event.stopPropagation();
    const menu = trigger.closest(".account-menu");
    const wasOpen = menu?.classList.contains("is-open");
    document.querySelectorAll(".account-menu.is-open").forEach((openMenu) => {
      if (openMenu !== menu) {
        openMenu.classList.remove("is-open");
        openMenu.querySelector("[data-account-trigger]")?.setAttribute("aria-expanded", "false");
      }
    });
    if (!menu) return;
    menu.classList.toggle("is-open", !wasOpen);
    trigger.setAttribute("aria-expanded", String(!wasOpen));
  });
});

document.addEventListener("click", (event) => {
  if (event.target.closest(".account-menu")) return;
  document.querySelectorAll(".account-menu.is-open").forEach((menu) => {
    menu.classList.remove("is-open");
    menu.querySelector("[data-account-trigger]")?.setAttribute("aria-expanded", "false");
  });
});

document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") return;
  document.querySelectorAll(".account-menu.is-open").forEach((menu) => {
    menu.classList.remove("is-open");
    menu.querySelector("[data-account-trigger]")?.setAttribute("aria-expanded", "false");
  });
});
