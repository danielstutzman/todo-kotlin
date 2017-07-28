import com.fizzed.rocker.Rocker
import spark.ModelAndView
import spark.TemplateEngine

class RockerEngine : TemplateEngine() {
  override fun render(modelAndView: ModelAndView): String {
    return Rocker.template(modelAndView.viewName)
        .bind(modelAndView.model as Map<String, Any>)
        .render()
        .toString()
  }
}
