import { useTheme } from "@/hooks/useTheme"
import { Toaster as Sonner } from "sonner"

type ToasterProps = React.ComponentProps<typeof Sonner>

const Toaster = ({ ...props }: ToasterProps) => {
  const { theme } = useTheme()

  return (
    <Sonner
      theme={theme as ToasterProps["theme"]}
      className="toaster group"
      toastOptions={{
        classNames: {
          toast:
            "group toast group-[.toaster]:bg-card group-[.toaster]:text-card-foreground group-[.toaster]:border-2 group-[.toaster]:border-border group-[.toaster]:shadow-xl group-[.toaster]:backdrop-blur-sm",
          description: "group-[.toast]:text-muted-foreground",
          actionButton:
            "group-[.toast]:bg-primary group-[.toast]:text-primary-foreground group-[.toast]:hover:bg-primary/90",
          cancelButton:
            "group-[.toast]:bg-muted group-[.toast]:text-muted-foreground group-[.toast]:hover:bg-muted/80",
          success:
            "group-[.toaster]:!bg-green-50 group-[.toaster]:!text-green-900 group-[.toaster]:!border-green-300 dark:group-[.toaster]:!bg-green-950 dark:group-[.toaster]:!text-green-50 dark:group-[.toaster]:!border-green-800",
          error:
            "group-[.toaster]:!bg-red-50 group-[.toaster]:!text-red-900 group-[.toaster]:!border-red-300 dark:group-[.toaster]:!bg-red-950 dark:group-[.toaster]:!text-red-50 dark:group-[.toaster]:!border-red-800",
          warning:
            "group-[.toaster]:!bg-yellow-50 group-[.toaster]:!text-yellow-900 group-[.toaster]:!border-yellow-300 dark:group-[.toaster]:!bg-yellow-950 dark:group-[.toaster]:!text-yellow-50 dark:group-[.toaster]:!border-yellow-800",
          info:
            "group-[.toaster]:!bg-blue-50 group-[.toaster]:!text-blue-900 group-[.toaster]:!border-blue-300 dark:group-[.toaster]:!bg-blue-950 dark:group-[.toaster]:!text-blue-50 dark:group-[.toaster]:!border-blue-800",
        },
      }}
      {...props}
    />
  )
}

export { Toaster }
