package views;

public class SignUpForm {
    public String email;
    public String password;
    public String passwordConfirmation;

    public SignUpForm(String email, String password, String passwordConfirmation) {
        this.email = email;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
    }
}
