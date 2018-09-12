package com.sassaworks.senseplanner;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(AndroidJUnit4.class)
public class CreateEventTest {


    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);


    @Before
    public void setupFragment()
    {
        //mActivityTestRule.getActivity().getSupportFragmentManager().beginTransaction();
        IdlingRegistry.getInstance().register(FirebaseIdlingResource.getIdlingResource());
    }

    @Test
    public void testOnActivityClick() throws InterruptedException {

        onView(ViewMatchers.withId(R.id.list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        //Thread.sleep(2000);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        String date = mActivityTestRule.getActivity().getResources().getString(R.string.date_format,
                calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.YEAR));

        onView(withId(R.id.et_date))
                .check(matches(isEditTextValueEqualTo(date)));

        onView(withId(R.id.sp_activities)).check(matches(withTextInSpinnerSelectedView(containsString("Hobby"))));


    }


    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(FirebaseIdlingResource.getIdlingResource());
    }

    Matcher<View> isEditTextValueEqualTo(final String content) {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Match Edit Text Value with View ID Value : :  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView) && !(view instanceof EditText)) {
                    return false;
                }
                if (view != null) {
                    String text;
                    if (view instanceof TextView) {
                        text =((TextView) view).getText().toString();
                    } else {
                        text =((EditText) view).getText().toString();
                    }

                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }



    //Took this method from https://gist.github.com/igorokr/5f3db37f0b9eb8b2feae
    //for getting selected spinner value
    public static Matcher<View> withTextInSpinnerSelectedView(final Matcher<String> stringMatcher) {

        return new BoundedMatcher<View, Spinner>(Spinner.class) {
            @Override
            public void describeTo(Description description) {
                stringMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(Spinner spinner) {
                return findTextRecursively(spinner.getSelectedView());
            }

            private boolean findTextRecursively(View view) {
                if (view instanceof ViewGroup) {
                    ViewGroup innerViewGroup = (ViewGroup)view;
                    for (int i = 0; i < innerViewGroup.getChildCount(); i++) {
                        View innerView = innerViewGroup.getChildAt(i);
                        if (innerView instanceof TextView) {
                            if (isTextMatch((TextView) innerView)) {
                                return true;
                            }
                        } else if (innerView instanceof ViewGroup) {
                            return findTextRecursively(innerView);
                        }
                    }
                } else if (view instanceof TextView) {
                    return isTextMatch((TextView) view);
                }
                return false;
            }

            private boolean isTextMatch(TextView textView) {
                String text = textView.getText().toString();
                if (stringMatcher.matches(text)) {
                    return true;
                }
                return false;
            }

        };
    }

}
