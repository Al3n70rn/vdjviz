package controllers;

import models.Account;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import com.avaje.ebean.Ebean;
import models.Account;
import models.UserFile;
import play.Play;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Controller;
import play.mvc.Results;
import play.mvc.Security;
import views.html.newuser;

import java.io.File;
import java.util.List;

public class Users extends Controller {

    private static final Form<User> userForm = Form.form(User.class);
    private static final Form<Account> accountForm = Form.form(Account.class);

    public static Result saveNewUser() {
        Form<User> userboundForm = userForm.bindFromRequest();
        Form<Account> accountboundForm = accountForm.bindFromRequest();
        if (userboundForm.hasErrors() || accountboundForm.hasErrors()) {
            //TODO
            return badRequest();
        }
        Account account = accountboundForm.get();
        User user = userboundForm.get();
        //TODO fake?
        User isUserExist = User.findByEmail(user.email);
        Account isAccountExist = Account.findByUserName(account.user_name);
        if (isUserExist == null && isAccountExist == null) {
            account.user = user;
            Ebean.save(user);
            Ebean.save(account);
            user.account = account;
            Ebean.update(user);
            String upload_path = Play.application().configuration().getString("file_upload_path");
            File temp_dir = new File(upload_path + account.user_name + "/");
            temp_dir.mkdir();
        } else {
            return ok("user is already exist");
        }
        flash("success", String.format("Successfully added user %s", user));
        return Results.redirect(routes.Application.index(null));
    }

    public static Result newUser() {
        return ok(newuser.render(userForm, accountForm));
    }

}