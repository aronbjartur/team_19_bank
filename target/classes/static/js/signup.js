document.addEventListener('DOMContentLoaded', function () {
    const sectionElement = document.querySelector('section');
    const signupForm = document.querySelector('form');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordError = document.getElementById('passwordError');

    // 1. FADE-IN ANIMATION
    if (sectionElement) {
        // Assume section is hidden by default (e.g., opacity: 0 in CSS)
        sectionElement.style.opacity = 0;
        setTimeout(() => {
            sectionElement.style.transition = 'opacity 1s ease-in-out';
            sectionElement.style.opacity = 1;
        }, 500);
    }

    // 2. FORM SUBMISSION HANDLER
    if (signupForm) {
        signupForm.addEventListener('submit', function (e) {

            // Check if native browser validation fails (for 'required' fields)
            if (!signupForm.checkValidity()) {
                // If browser validation fails, let the browser show its error messages
                e.preventDefault();

                // Add shake effect for visual feedback
                if (sectionElement) {
                    sectionElement.classList.add('shake');
                    setTimeout(() => {
                        sectionElement.classList.remove('shake');
                    }, 500);
                }
                return;
            }

            // Check if passwords match (Custom logic)
            if (passwordInput.value !== confirmPasswordInput.value) {
                e.preventDefault(); // Stop submission

                passwordError.style.display = 'block'; // Show error message

                // Add shake effect
                if (sectionElement) {
                    sectionElement.classList.add('shake');
                    setTimeout(() => {
                        sectionElement.classList.remove('shake');
                    }, 500);
                }

            } else {
                // Passwords match and all fields are valid
                passwordError.style.display = 'none'; // Hide error message

                // The form submission continues normally to Spring MVC (no e.preventDefault() needed here)
                // The form will now POST data to /signup
            }
        });

        // Hide password error when user starts typing again
        passwordInput.addEventListener('input', () => { passwordError.style.display = 'none'; });
        confirmPasswordInput.addEventListener('input', () => { passwordError.style.display = 'none'; });
    }
});
