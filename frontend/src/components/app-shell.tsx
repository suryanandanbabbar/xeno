"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  BarChart3,
  LogOut,
  Package,
  PieChart,
  Users,
  Megaphone,
  Brain,
  LineChart,
  Lightbulb,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/theme-toggle";
import { tokenStore } from "@/lib/auth/token-store";
import { cn } from "@/lib/utils";

const nav = [
  { href: "/dashboard", label: "Dashboard", icon: BarChart3 },
  { href: "/customers", label: "Customers", icon: Users },
  { href: "/orders", label: "Orders", icon: Package },
  { href: "/segments", label: "Segments", icon: PieChart },
  {
  href: "/campaigns",
  label: "Campaigns",
  icon: Megaphone,
},
{
  href: "/ai-audience",
  label: "AI Audience",
  icon: Brain,
},
{
  href: "/analytics",
  label: "Analytics",
  icon: LineChart,
},
{
  href: "/insights",
  label: "Insights",
  icon: Lightbulb,
}
];

export function AppShell({ title, eyebrow, children }: { title: string; eyebrow: string; children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  return (
    <main className="min-h-screen bg-background">
      <aside className="fixed inset-y-0 left-0 hidden w-64 border-r bg-card p-5 lg:block">
        <div className="text-xl font-semibold">XenoPilot</div>
        <nav className="mt-10 space-y-1">
          {nav.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center rounded-md px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-secondary hover:text-foreground",
                pathname === item.href && "bg-secondary text-foreground"
              )}
            >
              <item.icon className="mr-2 h-4 w-4" />
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>
      <section className="lg:pl-64">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b bg-background/90 px-4 backdrop-blur md:px-8">
          <div>
            <p className="text-sm text-muted-foreground">{eyebrow}</p>
            <h1 className="text-lg font-semibold">{title}</h1>
          </div>
          <div className="flex items-center gap-2">
            <ThemeToggle />
            <Button
              variant="outline"
              onClick={() => {
                tokenStore.clear();
                router.replace("/login");
              }}
            >
              <LogOut className="mr-2 h-4 w-4" />
              Sign out
            </Button>
          </div>
        </header>
        <div className="p-4 md:p-8">{children}</div>
      </section>
    </main>
  );
}
