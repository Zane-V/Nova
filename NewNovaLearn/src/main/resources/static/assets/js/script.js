// =========================================
// NOVA LEARNING - SCRIPT.JS
// =========================================

// ---------- API Configuration ----------
// This is the single place that defines where the backend lives.
// The Spring Boot backend runs on port 8081 by default (see
// backend/src/main/resources/application.properties).
const API_BASE_URL = "http://localhost:8081";

// =========================================
// NOVA LEARNING - SCRIPT.JS (PART 1)
// Animations & Navbar
// =========================================

// ---------- Scroll Reveal ----------

const reveals = document.querySelectorAll(".reveal");

function revealOnScroll() {

    reveals.forEach((element) => {

        const windowHeight = window.innerHeight;
        const elementTop = element.getBoundingClientRect().top;

        if (elementTop < windowHeight - 100) {
            element.classList.add("active");
        }

    });

}

window.addEventListener("scroll", revealOnScroll);
revealOnScroll();


// ---------- Counter Animation ----------

const counters = document.querySelectorAll(".counter");

let counterStarted = false;

function startCounters() {

    const section = document.querySelector(".cta-section");

    if (!section) return;

    const position = section.getBoundingClientRect().top;

    if (position < window.innerHeight - 100 && !counterStarted) {

        counters.forEach(counter => {

            const target = Number(counter.getAttribute("data-target"));

            let count = 0;

            const updateCounter = () => {

                const increment = target / 100;

                if (count < target) {

                    count += increment;

                    counter.innerText = Math.ceil(count);

                    setTimeout(updateCounter, 20);

                } else {

                    counter.innerText = target + "+";

                }

            };

            updateCounter();

        });

        counterStarted = true;

    }

}

window.addEventListener("scroll", startCounters);
startCounters();


// ---------- Typing Effect ----------

const words = [
    "Web Development",
    "Data Science",
    "Cyber Security",
    "Digital Marketing"
];

let wordIndex = 0;
let charIndex = 0;

const typingText = document.getElementById("typing-text");

function type() {

    if (!typingText) return;

    if (charIndex < words[wordIndex].length) {

        typingText.textContent += words[wordIndex].charAt(charIndex);

        charIndex++;

        setTimeout(type, 100);

    } else {

        setTimeout(erase, 1500);

    }

}

function erase() {

    if (!typingText) return;

    if (charIndex > 0) {

        typingText.textContent =
            words[wordIndex].substring(0, charIndex - 1);

        charIndex--;

        setTimeout(erase, 50);

    } else {

        wordIndex++;

        if (wordIndex >= words.length) {
            wordIndex = 0;
        }

        setTimeout(type, 500);

    }

}

if (typingText) {
    type();
}


// ---------- Navbar Scroll ----------

const navbar = document.getElementById("mainNavbar");

if (navbar) {

    window.addEventListener("scroll", () => {

        if (window.scrollY > 50) {
            navbar.classList.add("scrolled");
        } else {
            navbar.classList.remove("scrolled");
        }

    });

}

// ---------- Password Visibility Toggle (used on login/register pages) ----------

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".toggle-password").forEach((toggle) => {
        toggle.addEventListener("click", function () {
            const input = this.parentElement.querySelector("input");
            const icon = this.matches("i") ? this : this.querySelector("i");
            if (!input) return;
            const type = input.getAttribute("type") === "password" ? "text" : "password";
            input.setAttribute("type", type);
            if (icon) {
                icon.classList.toggle("fa-eye");
                icon.classList.toggle("fa-eye-slash");
            }
        });
    });
});
