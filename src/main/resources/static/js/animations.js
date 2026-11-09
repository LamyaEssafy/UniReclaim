/**
 * Premium counter animations for dashboard stats
 */

document.addEventListener('DOMContentLoaded', () => {
    // Select all stats cards elements to apply count up animation
    const counters = document.querySelectorAll('.counter-value');
    const speed = 200; // lower number = faster speed

    const animate = (counter) => {
        const target = +counter.getAttribute('data-target');
        const count = +counter.innerText;
        const increment = target / speed;

        if (count < target) {
            counter.innerText = Math.ceil(count + increment);
            setTimeout(() => animate(counter), 1);
        } else {
            counter.innerText = target;
        }
    };

    // Use IntersectionObserver to start counting only when visible
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const counterObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animate(entry.target);
                observer.unobserve(entry.target); // only animate once
            }
        });
    }, observerOptions);

    counters.forEach(counter => {
        // Set initial state to 0
        counter.innerText = '0';
        counterObserver.observe(counter);
    });
});
