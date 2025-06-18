export default {
  mounted(el) {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          el.classList.add('is-visible')
        }
      },
      {
        threshold: 0.1
      }
    )
    observer.observe(el)
  }
}
