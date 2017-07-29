package views;

public class SignUpErrors {
    public String email;
    public String password;
    public String passwordConfirmation;

    public boolean any() {
        return email != null ||
                password != null ||
                passwordConfirmation != null;
    }

    public SignUpErrors setEmail(String email) {
        this.email = email;
        return this;
    }

    public SignUpErrors setPassword(String password) {
        this.password = password;
        return this;
    }

    public SignUpErrors setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
        return this;
    }
}
